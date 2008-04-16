/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
 
//
// Implementation of vehicle class for "Freeway".
//

#include <stdlib.h>
#include <math.h>
#include <gtk/gtk.h>
#include "vehicle.h"
#include "vehicle_list.h"

    const double DELTA_T = 0.0000555; // 1/5 sec expressed in hours
    const double OPT_DT = 0.0005; // optimal buffer (in hrs) in front
    const double CAR_LEN = 0.004; // car length (in miles) (roughly 16 ft)
    const double BRAKE_DV = 4.0; // 20 mph / sec for 1/5 sec
    const int CAR_LENGTH = 8;
    const int CAR_WIDTH = 4;
    const int LANE_CHANGE_STABILITY = 30;
    const int STATE_STABILITY = 1;

#define MIN2(a,b) ((a)<(b) ? (a) : (b))

Vehicle::Vehicle(int i, int l, double p, double v) {
    classID = CLASS_VEHICLE;
    name_int = i;
    lane_num = l;
    position = p;
    velocity = v;
    state = VSTATE_MAINTAIN;
    max_speed = 100;
    xlocation = 0;
    ylocation = 0;
    change_state = 0;
    restrict_change = 0;
    absent_mindedness = 0;
}

void
Vehicle::recalc(Vehicle *in_front, const int limit, void *neighbors) {
    extern int randomize; // from traffic.C
    extern int width;

    // Update position based on velocity
    recalc_pos();

    // Choose new state based on velocities, distances, etc.
    //
    // Drivers don't change state every 1/5 sec.  Rather they do so every now
    // and then at random, but at least every STATE_STABILITYth time.
    if (!randomize || (rand() % STATE_STABILITY == 0) || (absent_mindedness > STATE_STABILITY)) {
        recalc_state(in_front, limit);
        absent_mindedness = 0;
    } else {
        absent_mindedness++;
    }

    // Check for lane change
    if (restrict_change) {
        restrict_change--;
    }

    if (state != VSTATE_CHANGE_LANE && state != VSTATE_CRASH &&
            randomize && !change_state &&
            (!restrict_change) && (xlocation > 100) && (width - xlocation > 100)) {
        check_lane_change(in_front, neighbors);
    }

    // Update velocity based on state
    recalc_velocity();
}


// Update position based on velocity
void
Vehicle::recalc_pos() {
    position += velocity * DELTA_T;
}

double
Vehicle::optimal_dist(Vehicle *in_front) {
    // Calculate optimal following distance based on my velocity and the 
    // difference in velocity from the car in front.
    double dv = in_front->vel() - velocity;

    return (OPT_DT * velocity + (0.5 * dv * dv * DELTA_T / BRAKE_DV));
}

void
Vehicle::recalc_state(Vehicle *in_front, const int limit) {
    // Don't change state if crashed or changing lanes
    if (state == VSTATE_CRASH ||
            state == VSTATE_CHANGE_LANE ||
            state == VSTATE_CHANGE_LEFT ||
            state == VSTATE_CHANGE_RIGHT)
        return;

    // Choose new state based on velocities, distances, etc.
    if (in_front) // There is a car in front of me.
    {
        double ratio, dp;
        extern double width_in_miles;

        // Find distance to car in front, possibly accounting for car in front
        // having turned the corner to the lane above.
        dp = in_front->rear_pos() - position;
        if (in_front->lane_num != lane_num) dp += width_in_miles;
        ratio = dp / this->optimal_dist(in_front);

        // Choose new state
        if (dp < 0.0) { // Check to see if I crashed into car ahead of me
            state = VSTATE_CRASH;
            velocity = 0;
            in_front->state = VSTATE_CRASH;
            in_front->position = position + in_front->vehicle_length();
            in_front->velocity = 0;
        } else if (dp < this->vehicle_length() / 2) { // Try to remain at least one...
            // car length behind car in front
            if (velocity > in_front->velocity) {
                state = VSTATE_BRAKE;
            } else {
                state = VSTATE_MAINTAIN;
            }
        } else if (ratio < 0.7) {
            state = VSTATE_BRAKE;
        } else if (ratio < 0.9 || velocity > limit_speed(limit)) {
            state = VSTATE_COAST;
        } else if (ratio > 1.1 && velocity < limit_speed(limit) * 0.98) {
            state = VSTATE_ACCELERATE;
        } else {
            state = VSTATE_MAINTAIN;
        }
    } else { // I am the lead car; nobody ahead
        double ratio = velocity / limit_speed(limit);

        if (ratio > 1.0) {
            state = VSTATE_COAST;
        } else if (ratio < 0.9) {
            state = VSTATE_ACCELERATE;
        } else {
            state = VSTATE_MAINTAIN;
        }
    }

    // Scenario 2 BUG: Missing Parens cause state to be set to VSTATE_MAX_SPEED
    // every time it's set to VSTATE_ACCELERATE
    // if ( state == VSTATE_ACCELERATE || state == VSTATE_MAINTAIN  // BUG
    if ((state == VSTATE_ACCELERATE || state == VSTATE_MAINTAIN) // ORIGINAL
            && velocity >= (max_speed - 1)) {
        state = VSTATE_MAX_SPEED;
    }
}

void
Vehicle::check_lane_change(Vehicle *in_front, void *neighbors) {
    //
    // Scan across list of neighbors.  If anyone is too close, just forget it.
    // If someone is somewhat close behind, and going faster,  just forget it.
    // If someone is somewhat close ahead,  and going slower,  just forget it.
    // Otherwise, go for a lane change.
    //
    // Note: The argument is passed as a void pointer to appease header files.  
    //       It is really a list pointer.
    //
#define BIG_NUM 999

    Vehicle *n_in_front = NULL;
    Vehicle *n_in_back = NULL;
    double d_in_front = BIG_NUM;
    double d_in_back = BIG_NUM;

    // Most of the time, don't do it
    if (rand() % LANE_CHANGE_STABILITY != 0) return;

    // Find closest neighbor in front and back
    for (List *l = ((List *) neighbors)->first(); l->hasValue(); l = l->next()) {
        Vehicle *neighbor = l->value();
        double dist = neighbor->pos() - position;

        if (dist < 0) { // neighbor is in back
            dist = this->rear_pos() - neighbor->position;
            if (d_in_back > dist) {
                d_in_back = dist;
                n_in_back = neighbor;
            }
        } else { // neighbor is in front
            dist = neighbor->rear_pos() - position;
            if (d_in_front > dist) {
                d_in_front = dist;
                n_in_front = neighbor;
            }
        }
    }

    // If there is nobody ahead of me in this lane, no reason to change.
    if (!in_front) {
        return;
    }

    // Find optimal following distance for me to follow car in front and car in 
    // back to follow me.
    double opt_in_front, opt_in_back;
    if (n_in_front) opt_in_front = this->optimal_dist(n_in_front);
    if (n_in_back) opt_in_back = n_in_back->optimal_dist(this);

    // If the vehicles around me are changing lanes, wait.
    if ((in_front && in_front ->state == VSTATE_CHANGE_LANE) ||
            (n_in_front && n_in_front->state == VSTATE_CHANGE_LANE) ||
    (n_in_back && n_in_back ->state == VSTATE_CHANGE_LANE)) {
        return;
    }

    // If the neighbor in front is going slower and I'm not maxed out, bail.
    if (n_in_front && n_in_front->velocity < velocity &&
    d_in_front < opt_in_front * 3 && state != VSTATE_MAX_SPEED) {
        return;
    }

    // If there is not enough room in front, bail.
    if (n_in_front && d_in_front < opt_in_front / 2) {
        return;
    }

    // If there is not enough room in back, bail.
    if (n_in_back && d_in_back < opt_in_back / 2) {
        return;
    }

    // If there is not a lot of room in front, and guy in front is slower, bail.
    if (n_in_front && d_in_front < opt_in_front &&
    n_in_front->velocity < velocity) {
        return;
    }

    // If there is not a lot of room in back, and guy in back is faster, bail.
    if (n_in_back && d_in_back < opt_in_back && n_in_back->velocity > velocity) {
        return;
    }

    state = VSTATE_CHANGE_LANE;
    change_state = 0; // just beginning the change
    restrict_change = 40; // disallow lane changes for 20 updates
}

void
Vehicle::recalc_velocity() {
    // Update velocity based on state
    switch (state) {
        case VSTATE_COAST: velocity *= 0.98;
            break;
        case VSTATE_BRAKE: velocity -= BRAKE_DV;
            break;
        case VSTATE_ACCELERATE: velocity += 1.00;
            break;
        case VSTATE_MAINTAIN: break;
        case VSTATE_CRASH: velocity = 0.00;
            break;
        case VSTATE_MAX_SPEED: velocity = max_speed;
            break;
        case VSTATE_CHANGE_LANE: break;
        case VSTATE_CHANGE_LEFT: break;
        case VSTATE_CHANGE_RIGHT: break;
    }
    if (velocity < 0.0) {
        velocity = 0.0;
    }
}

double
Vehicle::vehicle_length() {
    return CAR_LEN;
}

int
Vehicle::limit_speed(int limit) {
    return (MIN2(limit, max_speed));
}

void
Vehicle::draw(GdkDrawable *pix, GdkGC *gc, int x, int y,
        int direction_right, int scale, int xorg, int yorg, int selected) {
    this->xloc(x);
    this->yloc(y);

    // If I am heading to the right, then I need to draw brick to the left of 
    // front of car.  If I am heading left, draw brick to the right.
    if (direction_right) {
        x -= (CAR_LENGTH - 1);
    }

    int l = x * scale + xorg;
    int t = y * scale + yorg;
    int w = CAR_LENGTH * scale;
    int h = CAR_WIDTH * scale;

    // Draw brick
    gdk_draw_rectangle(pix, gc, TRUE, l, t, w, h);

    // Put red box around "current vehicle"
    if (selected) {
        draw_selection(pix, gc, l, t, w, h, scale);
    }
}

ostream & operator<<(ostream &o, const Vehicle &v) {
    o.setf(ios::scientific, ios::floatfield); // use scientific notation

    o << v.name_int << "\t";
    o << v.lane_num << "\t";
    o << v.position << "\t";
    o << v.velocity << "\t";
    o << v.max_speed << "\t";
    o << v.state << "\t";
    o << v.xlocation;

    return o;
}

istream & operator>>(istream &i, Vehicle &v) {
    i.setf(ios::scientific, ios::floatfield); // use scientific notation

    i >> v.name_int;
    i >> v.lane_num;
    i >> v.position;
    i >> v.velocity;
    i >> v.max_speed;
    // Type-checking work-around since int I/O is used for enums 
    int x;
    i >> x;
    v.state = (VState) x;
    i >> v.xlocation;

    return i;
}


// Local convenience function to draw selection box around "current vehicle".
// May be used by other vehicle classes.
void
Vehicle::draw_selection(GdkDrawable *pix, GdkGC *gc, int x, int y, int width, int height, int scale) {
    extern GdkColor *color_red, *color_white;
    extern int pixel_depth;

    int l = x - 2 * scale;
    int t = y - 2 * scale;
    int w = width + 4 * scale - 1;
    int h = height + 4 * scale - 1;

    gdk_gc_set_foreground(gc, (pixel_depth > 1) ? color_red : color_white);

    gdk_draw_rectangle(pix, gc, TRUE, l, t, w, scale); // top
    gdk_draw_rectangle(pix, gc, TRUE, l, t, scale, h); // left
    gdk_draw_rectangle(pix, gc, TRUE, l, t + h - scale, w, scale); //  bottom
    gdk_draw_rectangle(pix, gc, TRUE, l + w - scale, t, scale, h); // right
}
