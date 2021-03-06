package org.dclab.zk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.dclab.util.ZipTool;
import org.springframework.stereotype.Service;

@Service
public class GitLabService {

	public GitLabService() {
		// TODO Auto-generated constructor stub

	}
	public void upLoad(String path) throws InterruptedException{	
		int index=path.lastIndexOf("/");
		String Directory=path.substring(0, index);  //目录
		String fileName=path.substring(index+1, path.length());  //文件名
		String shellL="#!/bin/bash"+'\n'+
				"cd "+Directory+'\n'+
				"git pull "+'\n'+
				"git add "+fileName+'\n'+
				"git commit -m \"upload a "+fileName+" file\" "+fileName+'\n'+
				"git push origin master"+'\n'+
				"echo 'pwd'";
		String shPath=System.getProperty("project.root")+"script"+File.separator+"call_mkdir.sh";
		execShell(shellL,shPath);
	}
	public void download(String path) throws InterruptedException{
		int index = path.lastIndexOf("/");
		String Directory = path.substring(0, index); // 目录
		// String fileName=path.substring(index+1, path.length()); //文件名
		String shellL = "#!/bin/bash" + '\n' + "cd " + Directory + '\n' + "git pull " + '\n' + "echo 'pwd'";
		String shPath = System.getProperty("project.root") + "script" + File.separator + "call_pull.sh";
		execShell(shellL,shPath);
	}
	public void getIntegration(String TSStype,String MessageType,String SourceComponent,String DestinationConponent,
			String SourceComponentType,String DestinationConponentType,String IOname,String IOtype,HttpServletResponse response) throws InterruptedException, IOException{
		String Directory="/home/sy/env/apache-tomcat-7.0.79/webapps/ModelManage/files";
		String shellL = "#!/bin/bash" + '\n' + "cd " + Directory + '\n'
				+"rm -rf newDemo_TMP"+ '\n' 
				+ "git clone git@192.168.1.125:root/newDemo_TMP.git" + '\n' + "echo 'pwd'";
		String shPath = System.getProperty("project.root") + "script" + File.separator + "call_integration.sh";
		execShell(shellL,shPath);
		String dir = System.getProperty("project.root") + "files";
		String dir1 = dir + File.separator + "newDemo_TMP";
		
		String filename = "Demo.zip";
		response.setHeader("content-disposition", "attachment;filename="  
                + URLEncoder.encode(filename, "UTF-8"));
		
		Process process = null;
		String command1 = "python3 " + "cg.py "+ DestinationConponent+" "+SourceComponent+" "+MessageType+" "+TSStype+" "+IOname+" "+IOtype;
		System.out.println("command1:" + command1);
		process = Runtime.getRuntime().exec(command1, null, new File(dir1));
		process.waitFor();
		File file = new File(dir1);
		if (file.isDirectory()) {
			String src = dir1;
			String dst = dir + File.separator + "Demo.zip";
			ZipTool.compress(src, dst);
			try {
				// get your file as InputStream
				InputStream is = new FileInputStream(new File(dst));
				org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
				response.flushBuffer();
				is.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		String shellL2 = "#!/bin/bash" + '\n' + "cd " + Directory + '\n'
				+"rm -rf newDemo_TMP"+ '\n'  + "echo 'pwd'";
		execShell(shellL2,shPath);
	}
	public void execShell(String shellL,String shPath) throws InterruptedException{
		try {
			FileOutputStream fos = new FileOutputStream(shPath);
			fos.write(shellL.getBytes()); // 覆盖之前文件内容
			Process process = null;
			String command1 = "chmod 777 " + shPath; // 设置权限
			process = Runtime.getRuntime().exec(command1);
			process.waitFor();
			String command2 = "/bin/sh " + shPath;
			process = Runtime.getRuntime().exec(command2);
			process.waitFor();
			InputStreamReader ir = new InputStreamReader(process.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			String line;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
			ir.close();
			// fos.flush();
			fos.close();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
