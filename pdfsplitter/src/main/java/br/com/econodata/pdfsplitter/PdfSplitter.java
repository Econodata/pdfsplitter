package br.com.econodata.pdfsplitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;

/**
 *
 */
public class PdfSplitter{
	private static Logger log= Logger.getLogger(PdfSplitter.class);	
	
	private static String inputFolderStr;
	private static String outputFolderStr;
	
	
    public static void main( String[] args ){
    	DOMConfigurator.configure("log4j_splitter.xml");
		
		Map<String,String> prmMap= new HashMap<String,String>();
    	for (int i= 0; i < args.length; i++) {
    		log.info("arg: "+args[i]);
    		String[] prmSplit= args[i].split("=");
    		prmMap.put(prmSplit[0].trim(), prmSplit[1].trim());
		}
    	
    	inputFolderStr= prmMap.get("input");
    	outputFolderStr= prmMap.get("output");
    	
    	runFileTree();
 
    }
    
    
    /**
     * 
     * @param toSplitFile
     */
    public static void fileSplitter(File toSplitFile, File outputFolder){
    	try{
    		log.info("<<fileSplitter>> splitting file: "+toSplitFile.toString());
    		
    		InputStream inputStream= new FileInputStream(toSplitFile);
    		PdfReader pdfReader= new PdfReader(inputStream);
    		
    		for(int pdfPg= 1; pdfPg <= pdfReader.getNumberOfPages(); pdfPg++){
    			String outputPdfPgFileStr= outputFolder.getAbsolutePath()+File.separator+toSplitFile.getName().substring(0, toSplitFile.getName().indexOf(".pdf"))+"_"+String.format("%04d", pdfPg)+".pdf";
    			File outputPdfPgFile= new File(outputPdfPgFileStr);

            	Document document= new Document();
                FileOutputStream fileOutputStream= new FileOutputStream(outputPdfPgFile);
                PdfSmartCopy pdfSmartCopy= new PdfSmartCopy(document, fileOutputStream);
                
                document.open();
                
                pdfSmartCopy.addPage(pdfSmartCopy.getImportedPage(pdfReader, pdfPg));
                
                document.close();
    			
    			log.info("<<fileSplitter>> new pdf created: "+outputPdfPgFile.toString());
    		}
    	} catch (Exception e){
    		log.error("<<fileSplitter>> Exception" + e);
    	}
    }
    
    
    
    public static void runFileTree(){
    	try{
    	
	    	final File inputDir= new File(inputFolderStr);
	    	final File outputDir= new File(outputFolderStr);
	        final Path rootPath= inputDir.toPath();
	
	        // Walk thru mainDir directory
	        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>(){
	        	
	            private Pattern pattern = Pattern.compile("^(.*?)\\.pdf$");  //trocar filtro para .pdf
	
	            @Override
	            public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts) throws IOException {
	                if(pattern.matcher(path.toString()).matches()){
		        		log.info("<<runFileTree>> File é PDF: " + path.toString());
		        		final Path relativePath= rootPath.relativize(path);
		        		final Path outPutPath= outputDir.toPath().resolve(relativePath.getParent());
		        		
		        		Files.createDirectories(outPutPath);
		        		
		        		fileSplitter(path.toFile(), outPutPath.toFile());
	                	
	                }
	                return FileVisitResult.CONTINUE;
	            }
	
	            @Override
	            public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
	            	log.error("<<runFileTree>> visitFileFailed -- Exception" + exc);
	                //exc.printStackTrace();
	
	                // If the root directory has failed it makes no sense to continue
	                return path.equals(rootPath)? FileVisitResult.TERMINATE:FileVisitResult.CONTINUE;
	            }
	        	
	        });
    	} catch (Exception e){
    		log.error("<<runFileTree>> Exception" + e);
    	}
    }
    
    
}
