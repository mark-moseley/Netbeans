package org.netbeans.jellytools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.*;

/*
 * CollectBundleKeys.java
 *
 * Created on August 1, 2002, 9:36 AM
 */

/**
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class CollectBundleKeys {

    static final Pattern pat=Pattern.compile("Bundle.getString(Trimmed)?\\s*\\(\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"", Pattern.MULTILINE);
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        if (args.length<2) {
            System.out.println("Missing command-line arguments: <source directory> <output property file> !");
            System.exit(1);
        }
        StringBuffer sb=new StringBuffer();
        byte b[] = new byte[1000];
        int i;
        ArrayList dirs=new ArrayList();
        File sub[];
        dirs.add(new File(args[0]));
        TreeMap bundles=new TreeMap();
        String bundle, key;
        TreeSet keys;
        while (dirs.size()>0) {
            sub=((File)dirs.remove(0)).listFiles();
            for (int j=0; sub!=null&&j<sub.length; j++) {
                if (sub[j].isDirectory()) {
                    dirs.add(sub[j]);
                } else if (sub[j].getName().toLowerCase().endsWith(".java")) {
                    FileInputStream in=new FileInputStream(sub[j]);
                    while ((i=in.read(b))>=0) 
                        sb.append(new String(b, 0, i));
                    in.close();
                    Matcher m=pat.matcher(sb);
                    while (m.find()) {
                        bundle=m.group(2);
                        key=m.group(3);
                        if (bundles.containsKey(bundle)) {
                            ((TreeSet)bundles.get(bundle)).add(key);
                        } else {
                            keys=new TreeSet();
                            keys.add(key);
                            bundles.put(bundle, keys);
                        }
                    }
                }
            }
        }
        Iterator bi=bundles.keySet().iterator();
        Iterator ki;
        int bs=0,ks=0;
        PrintStream out=new PrintStream(new FileOutputStream(args[1]));
        while (bi.hasNext()) {
            bs++;
            bundle=(String)bi.next();
            ki=((TreeSet)bundles.get(bundle)).iterator();
            out.print(bundle+"=");
            out.print((String)ki.next());
            ks++;
            while (ki.hasNext()) {
                out.print(","+(String)ki.next());
                ks++;
            }
            out.println();
        }
        out.close();
        System.out.println("Finished "+String.valueOf(ks)+" keys from "+String.valueOf(bs)+" bundles.");
    }
    
}
