/*
 * @Author Bruce Martin
 * Created on 29/08/2005
 *
 * Purpose: writes "Line's" to a binary file
 */
package net.sf.JRecord.IO;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.sf.JRecord.Common.FieldDetail;
import net.sf.JRecord.Details.AbstractLine;
import net.sf.JRecord.Details.RecordDetail;


/**
 *  writes "Line's" to a binary file
 *
 * @author Bruce Martin
 *
 */
public class ContinuousLineWriter extends AbstractLineWriter {
    private OutputStream outStream = null;

    private boolean toInit = true;
    private byte fillByte = 0;
    

    /**
     * create binary line writer
     */
    public ContinuousLineWriter() {
        super();
    }


  

    /**
     * @see net.sf.JRecord.IO.AbstractLineWriter#open(java.io.OutputStream)
     */
    public void open(OutputStream outputStream) throws IOException {

        outStream = outputStream;
        if (! (outStream instanceof BufferedOutputStream)) {
        	outStream = new BufferedOutputStream(outStream);
        }
    }


    /**
     * @see net.sf.JRecord.IO.AbstractLineWriter#write(net.sf.JRecord.Details.AbstractLine)
     */
    public void write(AbstractLine line) throws IOException {

        if (outStream == null) {
            throw new IOException(AbstractLineWriter.NOT_OPEN_MESSAGE);
        } else if (line == null) {
        	return;
        }

        byte[] rec = line.getData();
        int pref = line.getPreferredLayoutIdx();
        int prefLength;
        
        if (pref < 0 || ((prefLength = findLength(pref, line)) == rec.length) ) {
        	outStream.write(rec);    	
        } else if (prefLength < rec.length) {
        	outStream.write(rec, 0, prefLength);
        } else {
        	if (toInit) {
        		toInit = false;

        		if ((! line.getLayout().isBinary()) ) {
					fillByte = line.getLayout().getSpaceByte();
        		}
        	}
    		outStream.write(rec);

    		for (int i = rec.length; i < prefLength; i++) {
    			outStream.write(fillByte);
    		}
        }
    }

    
	private int findLength(int pref, AbstractLine line) {
		
		RecordDetail rec = line.getLayout().getRecord(pref);
		if (rec.hasDependingOn()) {
			FieldDetail f =  rec.getField(rec.getFieldCount() - 1);
			int len = rec.calculateActualPosition(line, f.getDependingOnDtls(), f.getEnd() + 1) - 1;
			return len;
		}
		return rec.getLength();
	}


    /**
     * @see net.sf.JRecord.IO.AbstractLineWriter#close()
     */
    public void close() throws IOException {

        outStream.close();
        outStream = null;
    }
}
