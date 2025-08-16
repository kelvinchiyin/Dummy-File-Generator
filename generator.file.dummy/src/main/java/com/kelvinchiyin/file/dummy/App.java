package com.kelvinchiyin.file.dummy;

import java.io.IOException;

public class App 
{
    public static void main( String[] args ) throws IOException 
    {
    	int fileSize = 50 * 1024 ;
    	Generator generator = new Generator("50KB" , fileSize);
    	generator.createAndWriteDocx();
    	generator.createAndWritePptx();
    	generator.createAndWriteXlsx();
    	generator.createAndWritePdf();
    	generator.createAndWriteJpg();
    	
    }
}
