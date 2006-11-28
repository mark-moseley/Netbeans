package org.netbeans.installer.infra.server.admin.servlets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.netbeans.installer.infra.server.ejb.EngineManager;
import org.netbeans.installer.infra.server.ejb.RegistryManager;
import org.netbeans.installer.utils.exceptions.XMLException;

/**
 *
 * @author Kirill Sorokin
 * @version
 */
public class RunCommand extends HttpServlet {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final File   UPLOADS = new File("D:\\temp\\nbi-server\\uploads");
    private static final String UTF     = "UTF-8";
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    @EJB
    private RegistryManager registryManager;
    
    @EJB
    private EngineManager engineManager;
    
    /**
     * Initializes the servlet. Creates the uploads directory if it does not exist.
     */
    public void init() {
        UPLOADS.mkdirs();
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String command  = (String) request.getAttribute("command");
        
        String registry = null;
        String uid      = null;
        String version  = null;
        File   archive  = null;
        
        if (isMultiPartFormData(request)) {
            Map<String, Object> parameters = getParameters(request);
            
            registry = (String) parameters.get("registry");
            
            uid      = (String) parameters.get("uid");
            version  = (String) parameters.get("version");
            
            archive  = (File) parameters.get("archive");
        } else {
            registry = request.getParameter("registry");
            
            uid      = request.getParameter("uid");
            version  = request.getParameter("version");
        }
        
        try {
            if (command.equals("add-registry")) {
                registryManager.addRegistry(registry);
            }
            
            if (command.equals("remove-registry")) {
                registryManager.removeRegistry(registry);
            }
            
            if (command.equals("update-engine")) {
                engineManager.updateEngine(archive);
            }
            
            if (command.equals("update-component")) {
                URL    url    = new URL(request.getRequestURL().toString());
                String string = url.toString();
                
                String prefix = string.substring(0,
                        string.indexOf(url.getFile())) +
                        "/nbi/get-file?registry=" + registry + "&file=";
                
                registryManager.updateComponent(registry, archive, uid, version,
                        prefix);
            }
            
            if (command.equals("remove-component")) {
                registryManager.removeComponent(registry, uid, version);
            }
            
            if (command.equals("update-group")) {
                URL    url    = new URL(request.getRequestURL().toString());
                String string = url.toString();
                
                String prefix = string.substring(0,
                        string.indexOf(url.getFile())) +
                        "/nbi/get-file?registry=" + registry + "&file=";
                
                registryManager.updateGroup(registry, archive, uid, version,
                        prefix);
            }
            
            if (command.equals("remove-group")) {
                registryManager.removeGroup(registry, uid);
            }
            
            response.getWriter().write(
                    "The \"" + command + "\" command was successfully executed.");
            
        } catch (IOException e) {
            e.printStackTrace(new PrintWriter(response.getWriter()));
            
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        response.getWriter().close();
    }
    
    /**
     * Reads the servlet parameters passed in via multipart/form-data http request.
     * The resulting map would contain values of two types: String and File,
     * depending on the type of parameter.
     *
     * @param request
     *          The request for which to read the parameters.
     * @throws java.io.IOException
     *          If an I/O error happens.
     * @return
     *          The parameters map.
     */
    private Map<String, Object> getParameters(HttpServletRequest request)
            throws IOException {
        final Map<String, Object> parameters  = new HashMap<String, Object>();
        final ServletInputStream  input       = request.getInputStream();
        final String              boundary    = getBoundary(request);
        final String              endBoundary = boundary + "--";
        
        byte[]       buffer    = new byte[boundary.length() * 2];
        byte[]       remainder = null;
        OutputStream output    = null;
        String       name      = null;
        String       filename  = null;
        
        // read the servlet input stream line by line and react accordingly
        while (true) {
            int    length = read(input, buffer);
            String line = new String(buffer, 0, length, UTF);
            
            // if we've reached a boundary this means that the current parameter's
            // data stream finished, we should start parsing the next one
            if (line.startsWith(boundary)) {
                // first we finish with the previous parameter, if there was one. if
                // it was a string, we need to put it to the map, if it was a file,
                // we just need to close the stream
                if (output != null) {
                    output.close();
                    
                    if (parameters.get(name) == null) {
                        parameters.put(name,
                                ((ByteArrayOutputStream) output).toString(UTF));
                    }
                }
                
                // if this is the end - break the loop and return the parameters
                if (line.startsWith(endBoundary)) {
                    break;
                }
                
                // parse the parameter descriptor - we need to find out whether it is
                // a string or a file and the name of this parameter. descriptor may
                // be longer than our buffer is, thus we need to make sure a
                // complete line is read before proceeding any further
                String descriptor = new String(buffer, 0, read(input, buffer), UTF);
                while (descriptor.trim().equals(descriptor)) {
                    descriptor += new String(buffer, 0, read(input, buffer), UTF);
                }
                
                name     = getName(descriptor);
                filename = getFileName(descriptor);
                
                // initialize the target output stream for parameter's data and read
                // the remaining lines before parameter data (one for strings, two
                // for files)
                read(input, buffer);
                if (filename == null) {
                    output = new ByteArrayOutputStream();
                } else {
                    read(input, buffer);
                    
                    File file = File.createTempFile("upload", null, UPLOADS);
                    
                    parameters.put(name, file);
                    output = new FileOutputStream(file);
                }
                
                remainder = null;
                
                // once we've finished with the boundary - we should proceed
                // directly to the next line, since it won't make any sense to store
                // this data anywhere
                continue;
            }
            
            // we need to watch very carefully for EOLs, since the last one does not
            // really belong to the parameter's data, hence the mind-bending logic
            if (remainder != null) {
                output.write(remainder);
            }
            
            if (buffer[length - 1] == 10) {
                if ((length >= 2) && (buffer[length - 2] == 13)) {
                    remainder = new byte[]{13, 10};
                } else {
                    remainder = new byte[]{10};
                }
            } else if (buffer[length - 1] == 13) {
                if ((length >= 2) && (buffer[length - 2] == 10)) {
                    remainder = new byte[]{10, 13};
                } else {
                    remainder = new byte[]{13};
                }
            } else {
                remainder = new byte[0];
            }
            
            // write the parameter's data to the target output stream (would be
            // bytearrayoutputstream for strings and fileoutputstream for files)
            output.write(buffer, 0, length - remainder.length);
        }
        
        return parameters;
    }
    
    // entry desriptor parsing //////////////////////////////////////////////////////
    /**
     * Gets the value of "name" attribute from a multipart/form-data entry. A
     * shorthand for getAttribute(descriptor, "name").
     *
     * @param descriptor
     *          The descriptor string to parse.
     * @return
     *          The value of the "name" attribute of null, if this attribute is
     *          not present.
     */
    private String getName(String descriptor) {
        return getAttribute(descriptor, "name");
    }
    
    /**
     * Gets the value of "filename" attribute from a multipart/form-data entry. A
     * shorthand for getAttribute(descriptor, "filename").
     *
     * @param descriptor
     *          The descriptor string to parse.
     * @return
     *          The value of the "filename" attribute of null, if this attribute is
     *          not present.
     */
    private String getFileName(String descriptor) {
        return getAttribute(descriptor, "filename");
    }
    
    /**
     * Gets attrbutes' values for a multipart/form-data entry descriptor. The
     * descriptor is expected to have attributes in the form <name>="<value>".
     *
     * @param descriptor
     *          The descriptor string to parse.
     * @param name
     *          The name of the attribute.
     * @return
     *          The value of the attribute or null if this attribute is not present.
     */
    private String getAttribute(String descriptor, String name) {
        Matcher matcher = Pattern.compile(name + "=\"(.*?)\"").matcher(descriptor);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
    
    // request properties accessors /////////////////////////////////////////////////
    /**
     * Checks whether the request is multipart/form-data.
     *
     * @param request
     *          An HttpServletReequest object for which the check should be done.
     * @return
     *          True is the request is multipart/form-data, false otherwise.
     */
    private boolean isMultiPartFormData(HttpServletRequest request) {
        return request.getContentType().startsWith("multipart/form-data");
    }
    
    /**
     * Gets the boundary value of a multipart/form-data request.
     *
     * @param request
     *          An HttpServletRequest object for which the boundary value should be
     *          extracted.
     * @return
     *          The boundary value or null, if it cannot be obtained.
     */
    private String getBoundary(HttpServletRequest request) {
        Matcher matcher = Pattern.compile("boundary=(.*)$").matcher(request.getContentType());
        if (matcher.find()) {
            return "--" + matcher.group(1);
        } else {
            return null;
        }
    }
    
    // utility methods //////////////////////////////////////////////////////////////
    /**
     * Reads a line into the specified byte array. The data is read until a newline
     * character is encountered (all of "\n", "\r", "\n\r", "\r\n" are considered a
     * newline character) or a maximum number of bytes is read. Thus the clients
     * should not expect the result to be always a complete line. The amximum number
     * of bytes is dictated by the length of the byte array.
     *
     * This wrapper is introduced in order not to return -1, when a line cannot be
     * read, but to throw an IOException instead, since this situation is erroneous
     * anyway.
     *
     * @param input
     *          A ServletInputStream from which to read data.
     * @param buffer
     *          A byte array where to put the data
     * @throws java.io.IOException
     *          If an I/O error happens.
     * @return
     *          The number of bytes read.
     */
    private int read(ServletInputStream input, byte[] buffer) throws IOException {
        int length = input.readLine(buffer, 0, buffer.length);
        
        if (length == -1) {
            throw new IOException("Could not read a complete " +
                    "buffer without reaching an EOL or reached " +
                    "the end of stream prematurely");
        }
        
        return length;
    }
    
    private String getHostUrl(HttpServletRequest request) throws MalformedURLException {
        URL    url    = new URL(request.getRequestURL().toString());
        String string = url.toString();
        
        return string.substring(0, string.indexOf(url.getFile()));
    }
}
