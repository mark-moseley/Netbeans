/*
* Copyright 2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package sessions;

import javax.servlet.http.*;
import java.util.Vector;
import java.util.Enumeration;

public class DummyCart {
    Vector v = new Vector();
    String submit = null;
    String item = null;
    
    private void addItem(String name) {
	v.addElement(name);
    }

    private void removeItem(String name) {
	v.removeElement(name);
    }

    public void setItem(String name) {
	item = name;
    }
    
    public void setSubmit(String s) {
	submit = s;
    }

    public String[] getItems() {
	String[] s = new String[v.size()];
	v.copyInto(s);
	return s;
    }
    
    public void processRequest(HttpServletRequest request) {
	// null value for submit - user hit enter instead of clicking on 
	// "add" or "remove"
	if (submit == null) 
	    addItem(item);

	if (submit.equals("add"))
	    addItem(item);
	else if (submit.equals("remove")) 
	    removeItem(item);
	
	// reset at the end of the request
	reset();
    }

    // reset
    private void reset() {
	submit = null;
	item = null;
    }
}
