/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

#if !defined SYSTEM_H
#define SYSTEM_H

#include "module.h"
//#include <vector>

using namespace std;

// System is a collection of modules

class System {
public:
    System();
    System(const System& obj); //copy constructor
        
    Module& GetModule(int index) const;
    void AddModule(Module* module);
        
    int GetModuleCount() const;
    int GetSupportMetric() const;

private:
    vector <Module*> moduleList;
    int supportMetric;

friend ostream& operator<< (ostream&, const System&);
};

#endif //SYSTEM_H
