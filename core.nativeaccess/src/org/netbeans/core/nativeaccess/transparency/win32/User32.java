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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

/*
 * Original file is from http://jna.dev.java.net/
 */
package org.netbeans.core.nativeaccess.transparency.win32;

import com.sun.jna.FromNativeContext;
import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.netbeans.core.nativeaccess.transparency.win32.GDI32.RECT;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;

/** Provides access to the w32 user32 library.
 * Incomplete implementation to support demos.
 *  
 * @author  Todd Fast, todd.fast@sun.com
 * @author twall@users.sf.net
 */
public interface User32 extends W32API {

    User32 INSTANCE = (User32)
        Native.loadLibrary("user32", User32.class, DEFAULT_OPTIONS);
    
    HDC GetDC(HWND hWnd);
    int ReleaseDC(HWND hWnd, HDC hDC);

    int FLASHW_STOP = 0;
    int FLASHW_CAPTION = 1;
    int FLASHW_TRAY = 2;
    int FLASHW_ALL = (FLASHW_CAPTION|FLASHW_TRAY);
    int FLASHW_TIMER = 4;
    int FLASHW_TIMERNOFG = 12;
    
    public static class FLASHWINFO extends Structure {
        public int cbSize;
        public HANDLE hWnd;
        public int dwFlags;
        public int uCount;
        public int dwTimeout;
    }

    int IMAGE_BITMAP=0;
    int IMAGE_ICON=1;
    int IMAGE_CURSOR=2;
    int IMAGE_ENHMETAFILE=3;

    int LR_DEFAULTCOLOR     =0x0000;
    int LR_MONOCHROME       =0x0001;
    int LR_COLOR            =0x0002;
    int LR_COPYRETURNORG    =0x0004;
    int LR_COPYDELETEORG    =0x0008;
    int LR_LOADFROMFILE     =0x0010;
    int LR_LOADTRANSPARENT  =0x0020;
    int LR_DEFAULTSIZE      =0x0040;
    int LR_VGACOLOR         =0x0080;
    int LR_LOADMAP3DCOLORS  =0x1000;
    int LR_CREATEDIBSECTION =0x2000;
    int LR_COPYFROMRESOURCE =0x4000;
    int LR_SHARED           =0x8000;

    HWND FindWindow(String winClass, String title);
    int GetClassName(HWND hWnd, byte[] lpClassName, int nMaxCount);
    public static class GUITHREADINFO extends Structure {
        public int cbSize = size();
        public int flags;
        HWND hwndActive;
        HWND hwndFocus;
        HWND hwndCapture;
        HWND hwndMenuOwner;
        HWND hwndMoveSize;
        HWND hwndCaret;
        RECT rcCaret;
    }
    boolean GetGUIThreadInfo(int idThread, GUITHREADINFO lpgui);
    
    public static class WINDOWINFO extends Structure {
        public int cbSize = size();
        public RECT rcWindow;
        public RECT rcClient;
        public int dwStyle;
        public int dwExStyle;
        public int dwWindowStatus;
        public int cxWindowBorders;
        public int cyWindowBorders;
        public short atomWindowType;
        public short wCreatorVersion;
    }
    boolean GetWindowInfo(HWND hWnd, WINDOWINFO pwi);
    boolean GetWindowRect(HWND hWnd, RECT rect);
    int GetWindowText(HWND hWnd, byte[] lpString, int nMaxCount);
    int GetWindowTextLength(HWND hWnd);
    int GetWindowModuleFileName(HWND hWnd, byte[] lpszFileName, int cchFileNameMax);
    int GetWindowThreadProcessId(HWND hWnd, IntByReference lpdwProcessId);
    interface WNDENUMPROC extends StdCallCallback {
        /** Return whether to continue enumeration. */
        boolean callback(HWND hWnd, Pointer data);
    }
    boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer data);
    boolean EnumThreadWindows(int dwThreadId, WNDENUMPROC lpEnumFunc, Pointer data);

    boolean FlashWindowEx(FLASHWINFO info);

    HICON LoadIcon(HINSTANCE hInstance, String iconName);

    HANDLE LoadImage(HINSTANCE hinst,   // handle to instance 
                     String name,  // image to load 
                     int type,        // image type 
                     int xDesired,     // desired width 
                     int yDesired,     // desired height 
                     int load        // load options 
    );

    boolean DestroyIcon(HICON hicon);

    int GWL_EXSTYLE = -20;
    int GWL_STYLE = -16;
    int GWL_WNDPROC = -4;
    int GWL_HINSTANCE = -6;
    int GWL_ID = -12;
    int GWL_USERDATA = -21;
    int DWL_DLGPROC = 4;
    int DWL_MSGRESULT = 0;
    int DWL_USER = 8;
    int WS_EX_COMPOSITED = 0x20000000;
    int WS_EX_LAYERED = 0x80000;
    int WS_EX_TRANSPARENT = 32;
    int GetWindowLong(HWND hWnd, int nIndex);
    int SetWindowLong(HWND hWnd, int nIndex, int dwNewLong);
    // Do not use this version on win64
    Pointer SetWindowLong(HWND hWnd, int nIndex, Pointer dwNewLong);
    
    LONG_PTR GetWindowLongPtr(HWND hWnd, int nIndex);
    LONG_PTR SetWindowLongPtr(HWND hWnd, int nIndex, LONG_PTR dwNewLongPtr);
    Pointer SetWindowLongPtr(HWND hWnd, int nIndex, Pointer dwNewLongPtr);

    int LWA_COLORKEY = 1;
    int LWA_ALPHA = 2;
    int ULW_COLORKEY = 1;
    int ULW_ALPHA = 2;
    int ULW_OPAQUE = 4;
    boolean SetLayeredWindowAttributes(HWND hwnd, int crKey, 
                                       byte bAlpha, int dwFlags);
    boolean GetLayeredWindowAttributes(HWND hwnd,
                                       IntByReference pcrKey,
                                       ByteByReference pbAlpha,
                                       IntByReference pdwFlags);

    /** Defines the x- and y-coordinates of a point. */
    public static class POINT extends Structure {
        public int x, y;
    }
    /** Specifies the width and height of a rectangle. */
    public static class SIZE extends Structure {
        public int cx, cy;
    }
    int AC_SRC_OVER = 0x00;
    int AC_SRC_ALPHA = 0x01;
    int AC_SRC_NO_PREMULT_ALPHA = 0x01;
    int AC_SRC_NO_ALPHA = 0x02;
    public static class BLENDFUNCTION extends Structure {
        public byte BlendOp = AC_SRC_OVER; // only valid value
        public byte BlendFlags = 0; // only valid value
        public byte SourceConstantAlpha;
        public byte AlphaFormat;
    }
    boolean UpdateLayeredWindow(HWND hwnd, HDC hdcDst, 
                                POINT pptDst, SIZE psize, 
                                HDC hdcSrc, POINT pptSrc, int crKey, 
                                BLENDFUNCTION pblend, int dwFlags);
    int SetWindowRgn(HWND hWnd, HRGN hRgn, boolean bRedraw);
    int VK_SHIFT = 16;
    int VK_LSHIFT = 0xA0;
    int VK_RSHIFT = 0xA1;
    int VK_CONTROL = 17;
    int VK_LCONTROL = 0xA2;
    int VK_RCONTROL = 0xA3;
    int VK_MENU = 18;
    int VK_LMENU = 0xA4;
    int VK_RMENU = 0xA5;
    boolean GetKeyboardState(byte[] state);
    short GetAsyncKeyState(int vKey);

    int WH_KEYBOARD = 2;
    int WH_MOUSE = 7;
    int WH_KEYBOARD_LL = 13;
    int WH_MOUSE_LL = 14;
    public static class HHOOK extends HANDLE { }
    public static interface HOOKPROC extends StdCallCallback { }
    int WM_KEYDOWN = 256;
    int WM_KEYUP = 257;
    int WM_SYSKEYDOWN = 260;
    int WM_SYSKEYUP = 261;
    public static class KBDLLHOOKSTRUCT extends Structure {
        public int vkCode;
        public int scanCode;
        public int flags;
        public int time;
        public ULONG_PTR dwExtraInfo;
    }
    public static interface LowLevelKeyboardProc extends HOOKPROC {
        LRESULT callback(int nCode, WPARAM wParam, KBDLLHOOKSTRUCT lParam);
    }
    HHOOK SetWindowsHookEx(int idHook, HOOKPROC lpfn, HINSTANCE hMod, int dwThreadId);
    LRESULT CallNextHookEx(HHOOK hhk, int nCode, WPARAM wParam, LPARAM lParam);
    LRESULT CallNextHookEx(HHOOK hhk, int nCode, WPARAM wParam, Pointer lParam);
    boolean UnhookWindowsHookEx(HHOOK hhk);
    
    public static class MSG extends Structure {
        public HWND hWnd;
        public int message;
        public WPARAM wParam;
        public LPARAM lParam;
        public int time;
        public POINT pt;
    }
    int GetMessage(MSG lpMsg, HWND hWnd, int wMsgFilterMin, int wMsgFilterMax);
    boolean PeekMessage(MSG lpMsg, HWND hWnd, int wMsgFilterMin, int wMsgFilterMax, int wRemoveMsg);
    boolean TranslateMessage(MSG lpMsg);
    LRESULT DispatchMessage(MSG lpMsg);
    void PostMessage(HWND hWnd, int msg, WPARAM wParam, LPARAM lParam);
    void PostQuitMessage(int nExitCode);
}
