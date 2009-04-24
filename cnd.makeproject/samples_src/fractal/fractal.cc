/*
 * Copyright (c) 2009, Sun Microsystems, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the distribution.
 *  * Neither the name of Sun Microsystems, Inc. nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED 
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <sys/time.h>
#include <string.h>
#define wi 1000 //1280            // consider (wi,hi) as the R-axis and I-axis on a complex plane
#define hi 800 //1024
#define MAXISIZE 500000    // maximum iterated elements allowed to written per file

int hits[wi][hi]={0};
int max[wi][hi]={0};
double realBuf[wi][hi]={0};
double imgBuf[wi][hi]={0};
long double currLeft=-2, currRight=2, currTop=1.6, currBottom=-1.6, inc=.001;
long double wide=wi, high=hi, iter=100;

// Complex struct: overloads +, -, *, /, =, and implemented function cpow
struct complex {
    long double real;
    long double img;
    
    // overload basic complex operations
    complex(long double r, long double i) { real=r; img=i; }
    
    complex operator +(const complex& a){
        return complex(real+a.real, img+a.img);
    }
    complex operator -(const complex& a){
        return complex(real-a.real, img-a.img);
    }
    complex operator *(const complex& a){
        return complex( (real*a.real) - (img*a.img) , (real*a.img) + (img*a.real) );
    }
    complex operator /(const complex& a){
        return complex( ( ( real*a.real) + (img*a.img) ) / ( ( a.real * a.real ) + (a.img*a.img) ) ,
        ( ( (a.real*img) - (real*a.img) ) / ( (a.real*a.real) + (a.img*a.img) ) ) );
    }
    complex operator =(const complex& a){
        real=a.real;
        img=a.img;
        return *this;
    }
    complex cpow(long double a) {   // Complex exponentials
        // determine phase and amplitude
        long double logr = log(hypot(real, img));
        long double logi = atan2(img, real);
        long double x = exp(logr*a);
        long double y = logi*a;
        
        // Euclidean formula for complex nubmer
        real = x * cos(y);
        img = x * sin(y);
        
        return *this;
    }
};

double abs(complex& a){ // magnitude of the complex number
    return sqrt(a.real*a.real + a.img*a.img);
}

/* Note: Distinction between a Mandelbrot set and Buddhabrot set,
 * Buddhabrot is still a Mandelbrot set, but display the number of
 * iterations (i) at position z, instead of c.
 * Thus visually Buddhabrot does not sweep through the plane systematically.
 * Depends on inputs, increments, and locality, it might paint the complex
 * plane radially, with certain rotation.
 */
void Mandelbrot()   // Basic Mandelbrot calculation
{
    int i=0, x=0, y=0, fcnt=0, icnt=0;
    long double a=currLeft, b=currBottom;
    complex z(0, 0); complex c(0, 0);
    long double inc=(currRight-currLeft)/wide;
    long double yinc=(currTop-currBottom)/high;
    long double cnt = 0;
    char filename[80];
    strcpy(filename, "output_pns_0.dat");
    FILE* fh = fopen(filename, (char *)&"a+b");
    if(fh==NULL){
        printf("Cannot create output files\n");
        printf("Usage: Execute native/seq_demo2 from .../sampledir/C++/Fractal/Java\n");
        exit(0);
    }
    
    // Mandelbrot fractals: f(z)=z^2+c, where z, c are complex
    // Note: the exponents for z could be varied to achieve other types of fractals
    // Varying const. c by sweeping across the complex plane
    for(a=currLeft; a<currRight; a+=inc) {
        c.real=a;
        
        for(b=currBottom; b<currTop; b+=yinc) {
            i=0;
            z.real=0; z.img=0;
            c.img=b;
            
            // divergence test
            // (note 1) computation complexity, or resolution finess, is determined by pre-defined iteration limit, iter
            // (note 2) a graphic interface would take (n, z.real, z.img) and display the result
            while( abs(z)<2 && i<=iter) {
                
                z=(z*z)+c;  // heart-and-soul of the Mandelbrot fractal
                
                // check if z is inbound
                if(z.real>currLeft && z.real<currRight && z.img<currTop && z.img>currBottom && i!=0) {
                    
                    x=(z.real-currLeft)/inc;
                    y=(z.img-currBottom)/yinc;
                    hits[x][y]++;
                    
                    if(hits[x][y]>max[x][y]){
                        max[x][y]=hits[x][y];
                        realBuf[x][y]=z.real;
                        imgBuf[x][y]=z.img;
                        icnt++;
                    }
                    
                    if( (icnt/MAXISIZE) >0){
                        icnt = 0;
                        if(fcnt>0){
                            fclose(fh);
                            remove(filename);
                            
                            fh = fopen(filename, (char *)&"ab");
                        }
                        
                        // write to n-th output files
                        for(int ii=0; ii<wi; ii++){
                            for(int jj=0; jj<hi; jj++){
                                if(max[ii][jj]>0)
                                    fprintf(fh, "%d %lf %lf\n", max[ii][jj], (float)realBuf[ii][jj], (float)imgBuf[ii][jj]);
                            }
                        }
                        fcnt++;
                    }
                }
                i++;
            }
        }
    }
    fprintf(fh, "%c %d \n", 'e', fcnt); // indicates end of all outputs to java
    fclose(fh);
}

#define USEC_TO_SEC 1.0e-6
double wallTime(){
    double seconds;
    struct timeval tv;

    gettimeofday(&tv, NULL);

    seconds = (double) tv.tv_sec; // seconds since Jan. 1, 1970
    seconds += (double)tv.tv_usec * USEC_TO_SEC; // and microseconds
    return seconds;
}


int main(int argc, char* argv[]) {
    printf("Calculating. Please wait....\n");
    // tracking  wall time
    double startwtime = 0.0;
    double endwtime;
    startwtime = wallTime();
    
    // initialize golbal vectors
    for(int i=0; i<wi; i++){
        for(int j=0; j<hi; j++){
            max[i][j] = 0;
            realBuf[i][j] = 0;
            imgBuf[i][j] = 0;
        }
    }
    
    // call Mantelbrot routine
    Mandelbrot();
    
    // calculate wall time
    endwtime = wallTime();
    double total = (double) endwtime-startwtime;
    printf("Wall clock time = %lf seconds\n", total);
    return 0;
}
