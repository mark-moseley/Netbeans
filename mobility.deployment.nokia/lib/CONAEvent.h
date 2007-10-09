// Copyright 2005, 2007 Nokia Corporation. All rights reserved.
//
// The contents of this file are subject to the terms of the Common
// Development
The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

#include "CONADefinitions.h"
#include <jni.h>

#ifndef __NDSJMETOCONA_H__
#define __NDSJMETOCONA_H__

DWORD CALLBACK DeviceNotifyCallback(DWORD dwStatus, WCHAR* pstrSerialNumber);
DWORD CALLBACK FileOperationNotifyCallback(DWORD dwFSFunction, DWORD dwState, DWORD dwTransferredBytes, DWORD dwAllBytes);

//-----------------------------------------------------------------------------
// CONAEventHandler declaration
//-----------------------------------------------------------------------------
class CONAEventHandler
{
private:
	JNIEnv* m_pEnv;
	jobject m_object;
	JavaVM* m_pVM;

	int setCurrentFolderStatus;
	int createFolderStatus;
	int putFileStatus;
	int installFileStatus;

public:
	CONAEventHandler(JNIEnv* jniEnv, jobject obj);
	~CONAEventHandler();
	
	JNIEnv* getJavaEnv();
	jobject getJavaObject();
	JavaVM* getJavaVM();
	int getState(int type);
	void setState(int state, int type);
	void connectionLost();
};

#endif //__NDSJMETOCONA_H__