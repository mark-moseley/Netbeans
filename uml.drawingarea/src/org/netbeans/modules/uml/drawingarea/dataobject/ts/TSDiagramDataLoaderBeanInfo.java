/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.uml.drawingarea.dataobject.ts;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;
import org.openide.loaders.UniFileLoader;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

public class TSDiagramDataLoaderBeanInfo extends SimpleBeanInfo
{

    @Override
    public BeanInfo[] getAdditionalBeanInfo()
    {
        try
        {
            return new BeanInfo[]{Introspector.getBeanInfo(UniFileLoader.class)};
        }
        catch (IntrospectionException e)
        {
            throw new AssertionError(e);
        }
    }

    @Override
    public Image getIcon(int type)
    {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16)
        {
            return ImageUtilities.loadImage("org/netbeans/modules/uml/drawingarea/dataobject/ts/diagram.png");
        }
        else
        {
            return null;
        }

    }
}
