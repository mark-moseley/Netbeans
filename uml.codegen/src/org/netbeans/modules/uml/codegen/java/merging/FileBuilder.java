/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.codegen.java.merging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *  The class that perform actual merge of source files
 *
 */
public class FileBuilder
{
   private static final String SPACE = " ";
   private static final String NEWLINE ="\n";
   private static final int BUFFER_SIZE= 256;
   private File newFile;
   private File oldFile;
   
   private RandomAccessFile raOldFile;
   private RandomAccessFile raNewFile;
   private PositionMapper posMapper = null;
   private long originalTopPos = 0;
   private long originalBottomPos = 0;
   
   public FileBuilder(String newFilename, String oldFilename)
   {
      if ( newFilename != null && newFilename.length() > 0 )
      {
         newFile = new File(newFilename);
      }
      
      if ( oldFilename != null && oldFilename.length() > 0 )
      {
         oldFile = new File(oldFilename);
      }
      
      init(newFile, oldFile);
   }
   
   private void init(File newFile, File oldFile)
   {
      posMapper = new PositionMapper();
      
      try
      {
         if (oldFile != null)
         {
            //Open files for random access
            raOldFile = new RandomAccessFile(oldFile, "rw");    // for read only
            originalTopPos = this.getSrcTopPosition();
            originalBottomPos = this.getSrcBottomPosition();
         }
         
         if (newFile != null)
         {
            raNewFile = new RandomAccessFile(newFile, "r");    // for read only
         }
      }
      catch (FileNotFoundException ex)
      {
         //TODO: Display proper error message using NB style
         ex.printStackTrace();
      }
      catch (Exception ex2)
      {
         //TODO: Display proper error message using NB style
         ex2.printStackTrace();
      }
   }
   
   /**
    *  client calls this method to indicate that text fragment representing
    *  oldElem in the old file should be replaced by text fragment representing
    *  newElem taken from new file
    * @param newElem
    * @param oldElem
    */
   public void replace(ElementDescriptor newElem, ElementDescriptor oldElem)
   {
      if (newElem == null)
         return;
      
      long oldElemStartPos = getElemStartPosition(oldElem);
      long oldElemEndPos = getElemEndPosition(oldElem);
      
      long newElemStartPos = getElemStartPosition(newElem);
      long newElemEndPos = getElemEndPosition(newElem);
      
      
      byte[] replacingBuffer = null;
      int numOfBytesReplaced = 0;
      
      try
      {
         if (newElemStartPos > -1 && newElemEndPos > -1 &&
               newElemStartPos < newElemEndPos)
         {
            int newDataSize = (int)(newElemEndPos - newElemStartPos + 1);
            replacingBuffer = new byte[newDataSize];
            
            if (raNewFile != null && raNewFile.length() > 0)
            {
               // read 'newDataSize' bytes from the new file starting at 'newElemStartPos'
               raNewFile.seek(newElemStartPos);
               numOfBytesReplaced = raNewFile.read(replacingBuffer);
            }
            
            if (oldElemStartPos > -1 && oldElemEndPos > -1 &&
                  oldElemStartPos < oldElemEndPos)
            {
               int oldDataSize = (int) (oldElemEndPos - oldElemStartPos + 1);
               long mappedStartPos = posMapper.getMappedPositionFor(oldElemStartPos);
               long mappedEndPos = posMapper.getMappedPositionFor(oldElemEndPos);
               
               long oldFileLen = this.raOldFile.length();
               // Copy the data in the old file staring from 'oldElemEndPos + 1' till
               // the EOF to a temporary file
               File tempFile = this.createTempFile(FileUtil.toFileObject(oldFile).getName());
               if (tempFile != null)
               {
                  RandomAccessFile tempOutput = new RandomAccessFile(tempFile, "rw");
                  byte byteBuffer[] = new byte[this.BUFFER_SIZE];
                  int bytesRead = 0;
                  raOldFile.seek(mappedEndPos + 1);
                  while (bytesRead  != -1)   // loop till EOF is reached
                  {
                     bytesRead = raOldFile.read(byteBuffer);
                     if (bytesRead > 0)
                     {
                        tempOutput.write(byteBuffer, 0, bytesRead);
                     }
                  }
                  // In the old file, replace the old Data with the new data starting
                  // from the 'oldElemStartPos'
                  
                  raOldFile.seek(mappedStartPos);
                  raOldFile.write(replacingBuffer, 0, numOfBytesReplaced);
                  // write the data previously saved in the temporay file to the end
                  // of the old file.
                  bytesRead = 0;
                  tempOutput.seek(0);
                  while (bytesRead != -1)
                  {
                     bytesRead = tempOutput.read(byteBuffer);
                     if (bytesRead > 0)
                     {  // wrtie 'bytesRead' to the old file at the current position
                        // of the file pointer
                        raOldFile.write(byteBuffer, 0, bytesRead);
                     }
                  }
                  // Done replacing.Reset the length of the old file to the position
                  // of the current file pointer.
                  // Close and delete the temporaty file
                  long currentLen = raOldFile.getFilePointer();
                  if (oldFileLen != currentLen)
                  {
                     raOldFile.setLength(currentLen);
                     int bytesShift = newDataSize - oldDataSize;
                     if (bytesShift != 0) 
                     {  
                        // keep track of changes made to the old file
                        this.posMapper.addToMap(oldElemStartPos, bytesShift);
                     }
                  }
                  
                  tempOutput.close();
                  tempFile.delete();
               }
            }
         }
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
         completed();
      }
   }
   
   
   
   /**
    *  client calls this method to indicate that text fragment representing
    *  oldElem in old file should be removed from the old file.
    * @param oldElem
    */
   
   public void remove(ElementDescriptor oldElem)
   {
      try
      {
         long fileLen = raOldFile == null ? -1 : raOldFile.length();
         if (oldElem == null || fileLen <= 0)
         {
            return;
         }
         
         long startPos = getElemStartPosition(oldElem);
         long endPos = getElemEndPosition(oldElem);
         
         if (startPos > -1 && endPos > -1 &&
               startPos < endPos && fileLen >= endPos)
         {
            int removedBytes = (int) (endPos - startPos + 1);
            long mappedStartPos = posMapper.getMappedPositionFor(startPos);
            long mappedEndPos = posMapper.getMappedPositionFor(endPos);
            
            // Copy the data in the old file staring from 'mappedEndPos + 1' till
            // the EOF to a temporary file
            File tempFile = this.createTempFile(FileUtil.toFileObject(oldFile).getName());
            if (tempFile != null)
            {
               RandomAccessFile tempOutput = new RandomAccessFile(tempFile, "rw");
               byte byteBuffer[] = new byte[BUFFER_SIZE];
               int bytesRead = 0;
               raOldFile.seek(mappedEndPos + 1);
               while (bytesRead  != -1)   // loop till EOF is reached
               {
                  bytesRead = raOldFile.read(byteBuffer);
                  if (bytesRead > 0)
                  {
                     tempOutput.write(byteBuffer, 0, bytesRead);
                  }
               }
               
               // Rewrite the data previously saved in the temporay file to the
               // file starting from the 'mappedStartPos'
               String dataStr = null;
               int count = 0;
               bytesRead = 0;
               byte outBuffer[] = null;
               
               tempOutput.seek(0);
               raOldFile.seek(mappedStartPos);
               while (bytesRead != -1)
               {
                  bytesRead = tempOutput.read(byteBuffer);
                  if (bytesRead > 0)
                  {
                     // strip off the leading spaces (not the trailing spaces) of
                     // the 1st patch of data read to prevent  multiple empty lines
                     if (count == 0)
                     {
                        char aChar = ' ';
                        dataStr = new String(byteBuffer, 0, bytesRead);
                        int strLen = dataStr.length();
                        for (int i = 0; i < strLen; i++)
                        {
                           aChar = dataStr.charAt(i);
                           if (!Character.isWhitespace(aChar))
                           {
                              dataStr = dataStr.substring(i);
                              break;
                           }
                        }
                        if (dataStr.length() != strLen)
                        {
                           outBuffer = dataStr.getBytes();
                           int removedSpaces = bytesRead - outBuffer.length;
                           if (removedSpaces > 0)
                           {
                              //update the total number of bytes being removed
                              removedBytes += removedSpaces;
                           }
                           raOldFile.write(outBuffer, 0, outBuffer.length);
                        }
                     }
                     else
                     {
                        raOldFile.write(byteBuffer, 0, bytesRead);
                     }
                  }
                  count++;
               }
               // Done removing. Reset the length of the old file to the position
               // of the current file pointer.
               // Close and delete the temporaty file
               long currentLen = raOldFile.getFilePointer();
               if (fileLen > currentLen)
               {
                  raOldFile.setLength(currentLen);
                  // keep track of changes made to the old file
                  this.posMapper.addToMap(startPos, -removedBytes);
                  
               }
               tempOutput.close();
               tempFile.delete();
            }
         }
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
         completed();
      }
   }
   
   /**
    *  client calls this method to indicate that text fragment representing
    *  newElem in new file should be added to the old file
    * @param newElem
    */
   public void add(ElementDescriptor newElem)
   {
      if (newElem == null)
         return;
      
      long startPos = getElemStartPosition(newElem);
      long endPos = getElemEndPosition(newElem);
      int bytesAdded = 0;
      try
      {
         if (startPos > -1 && endPos > -1 && startPos < endPos)
         {
            int addedSize = (int)(endPos - startPos + 1);
            byte addedBuffer[] = new byte[addedSize];
            
            // read 'addedBytes' bytes from the new src file starting from 'startPos'
            if (raNewFile != null && raNewFile.length() > 0)
            {
               raNewFile.seek(startPos);
               bytesAdded = raNewFile.read(addedBuffer);
            }
            
            if ( raOldFile != null)
            {
               long addPos = getInsertPosition(newElem);
               if (addPos != -1)
               {
                  // in old src file, save bytes from the 'addPos' till the end of
                  // the file to a temporaty file before inserting the new entry
                  File tempFile = this.createTempFile(FileUtil.toFileObject(oldFile).getName());
                  if (tempFile != null)
                  {
                     RandomAccessFile tempOutput = new RandomAccessFile(tempFile, "rw");
                     byte byteBuffer[] = new byte[this.BUFFER_SIZE];
                     int bytesRead = 0;
                     raOldFile.seek(addPos);
                     while (bytesRead  != -1)   // loop till EOF is reached
                     {
                        bytesRead = raOldFile.read(byteBuffer);
                        if (bytesRead > 0)
                        {
                           tempOutput.write(byteBuffer, 0, bytesRead);
                        }
                     }
                     
                     // Write the 'addedBuffer' to the old src file at 'addPos' position
                     // Insert indentation in front and newline at the end
                     byte indent[] = this.getIndentation(newElem);
                     addedSize += indent.length;
                     raOldFile.seek(addPos);
                     raOldFile.write(indent, 0 , indent.length);
                     raOldFile.write(addedBuffer, 0 , bytesAdded);
                     if (newElem.getModelElemType().equals("Operation"))
                     {
                        byte newlines[] = NEWLINE.concat(NEWLINE).getBytes();
                        raOldFile.write(newlines, 0 , newlines.length);
                        addedSize += newlines.length;
                     }
                     
                     // write the data previously saved in the temporay file to
                     // the old file.
                     bytesRead = 0;
                     tempOutput.seek(0);
                     while (bytesRead != -1)
                     {
                        bytesRead = tempOutput.read(byteBuffer);
                        if (bytesRead > 0)
                        {
                           raOldFile.write(byteBuffer, 0, bytesRead);
                        }
                     }
                     // keep track of changes made to the old file
                     posMapper.addToMap(addPos, addedSize);
                     
                     // Done Adding.
                     // Close and delete the temporaty file
                     tempOutput.close();
                     tempFile.delete();
                  }
               }
            }
         }
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
         completed();
      }
   }
   
   /**
    *  client calls this method to indicate that it finished
    *  with posting of the requests, and on return from this method
    *  it is expected that the [old]file on disk is modified
    *  according to all previously posted requests.
    */
   public void completed()
   {
      try
      {
         if (this.raOldFile != null)
         {
            this.raOldFile.close();
         }
         if (this.raNewFile != null)
         {
            this.raNewFile.close();
         }
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
      }
   }
   
   private long getInsertPosition(ElementDescriptor elem)
         throws IOException
   {
      String modelElemType = elem.getModelElemType();
      long insertPos = -1;
      if ("Attribute".equals(modelElemType))
      {
         insertPos = posMapper.getMappedPositionFor(originalTopPos);
      }
      else if ("Operation".equals(modelElemType))
      {
         insertPos = posMapper.getMappedPositionFor(this.originalBottomPos);
      }
      return insertPos;
   }
   
   // Returns the position of the byte left next to the last right brace.
   // In case it is not found, the position of the byte right next to the first
   // non-white-space character is returned.
   private long getSrcBottomPosition() throws IOException
   {
      int bufferSize = 2;
      long offset = 0;
      long fileLen = raOldFile == null ? -1 : raOldFile.length();
      if (fileLen > 0)
      {
         //find the last right brace in the file
         byte byteBuffer[] = new byte[bufferSize];
         int i = 0;
         boolean done = false;
         boolean closeBracketFound = false;
         char aChar = ' ';
         while (!done)
         {
            offset = fileLen - (bufferSize*++i);
            if ( offset >= 0)
            {
               raOldFile.seek(offset);
               // read up to 'bufferSize' bytes of data starting from the
               // 'offset' postion
               int bytesRead = raOldFile.read(byteBuffer);
               if ( bytesRead > 0) // sonmething is read
               {
                  // convert byte buffer to a string
                  String dataStr = new String(byteBuffer, 0, bytesRead);
                  // find the 1st non white space
                  for (int j = bytesRead-1; j >= 0 ; j--)
                  {
                     aChar = dataStr.charAt(j);
                     if (!Character.isWhitespace(aChar))
                     {
                        closeBracketFound = (aChar == '}');
                        done = true;
                        break;
                     }
                  }
               }
            }
         }
         offset = (closeBracketFound ? offset : raOldFile.getFilePointer());
      }
      return offset;
   }
   
   // Returns the position of the byte right next to the first left brace.
   // In case, the left brace is not found, 0 is returned.
   private long getSrcTopPosition() throws IOException
   {
      int bufferSize = 2;
      long offset = 0;
      long fileLen = raOldFile == null ? -1 : raOldFile.length();
      if (fileLen > 0)
      {
         //find the first left brace in the file
         byte byteBuffer[] = new byte[bufferSize];
         char aChar = ' ';
         boolean done = false;
         raOldFile.seek(offset);  // start reading from the top
         while (!done)
         {
            if (raOldFile.getFilePointer() < fileLen)
            {
               int bytesRead = raOldFile.read(byteBuffer);
               if ( bytesRead > 0) // sonmething is read
               {
                  // convert byte buffer to a string
                  String dataStr = new String(byteBuffer, 0, bytesRead);
                  // find the 1st left brace
                  for (int j = 0; j < dataStr.length() ; j++)
                  {
                     aChar = dataStr.charAt(j);
                     if (aChar == '{')
                     {
                        offset = raOldFile.getFilePointer();
                        done = true;
                        break;
                     }
                  }
               }
            }
         }
         offset = (offset == fileLen ? 0 : offset);
      }
      return offset;
   }
   
   
   private byte[] getIndentation(ElementDescriptor elem)
   {
      String indent = "";
      if ( elem != null)
      {
         int noOfSpaces = elem.getColumn("StartPosition")-1;
         
         for (int i=0; noOfSpaces > 0 && i < noOfSpaces ; i++)
         {
            indent += SPACE;
         }
      }
      return indent.getBytes();
   }
   
   private long getElemStartPosition(ElementDescriptor elem)
   {
      long startPos = elem.getStartPos();
      //TODO: check for comment, if exist, add comment to startPos
      return startPos;
   }
   
   private long getElemEndPosition(ElementDescriptor elem)
   {
      String modelElemType = elem.getModelElemType();
      long endPos = -1;
      if ("Attribute".equals(modelElemType))
      {
         long initValPos = elem.getPosition("InitialValue");
         if (initValPos  == -1 )   // no initial value
         {
            endPos = elem.getPosition("Name") +
                  elem.getLength("Name");        // including the ending ';'
         }
         else
         {
            endPos = initValPos + elem.getLength("InitialValue"); // including the ending ';'
         }
      }
      else if ("Operation".equals(modelElemType))
      {
         // Currently the element descriptor of a constructor does not include
         // end position. Return -1 for now.
         boolean isConstructor = Boolean.parseBoolean(
               elem.getModelElemAttribute("isConstructor"));
         if (isConstructor)
         {
            endPos = -1;
         }
         else
         {
            endPos = elem.getEndPos();
         }
      }
      return endPos;
   }
   
   
   // create an temp file in the system default temporary file folder
   private File createTempFile(String fileNameNoExt)
         throws IOException
   {
      File tempFile = null;
      if (fileNameNoExt != null && fileNameNoExt.length() > 0)
      {
         tempFile = File.createTempFile(fileNameNoExt, null, null);
      }
      return tempFile;
   }
}
