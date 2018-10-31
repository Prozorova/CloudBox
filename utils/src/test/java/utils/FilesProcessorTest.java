package utils;

import java.nio.file.Paths;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Ignore;
import org.junit.Test;

import com.cloud.utils.processors.FilesProcessor;

public class FilesProcessorTest {
	
	private static final FilesProcessor processor = new FilesProcessor();

	@Test
	public void testFilesGathering() {
		
		Set<String> set = null;
		try {
			set = processor.gatherFilesFromDir(Paths.get("/Server/ttt/").toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		set.stream().forEach(new Consumer<String>() {

			@Override
			public void accept(String t) {
				
				System.out.println(t);
			}
		});
	}
	
	@Test
	@Ignore
	public void testDeleteFile() {
		
		try {
			processor.deleteFile("/Server/ttt/hhh");
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Test
	@Ignore
	public void testMoveFile() {
		
		try {
//			processor.moveFile("/Server/ttt/copy.png", "/Server/ttt/copy47305.png");
//			processor.moveFile("/Server/ttt/share.png", "/Server/ttt/hhh/hhh/share.png");
//			processor.moveFile("/Server/ttt/directory.png", "/Server/ttt/directory.png");
//			processor.moveFile("/Server/ttt/delete.png", "/Server/ttt/mmm/vvv/delete.png");
			
			processor.moveFile("/Server/ttt/delete.png", "/Server/ttt/hhh/delete.png");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
