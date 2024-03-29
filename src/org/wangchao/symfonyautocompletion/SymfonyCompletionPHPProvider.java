/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wangchao.symfonyautocompletion;

import java.io.BufferedReader;
import java.util.concurrent.ExecutionException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author wangchao
 */
@MimeRegistration(mimeType = "text/x-php5", service = CompletionProvider.class)
public class SymfonyCompletionPHPProvider implements CompletionProvider {

    public static ArrayList<File> getSourceFolders(Project project) {
        ArrayList<File> result = new ArrayList<File>();
        if (project == null) {
            return result;
        }
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup sourceGroups[] = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int i = 0; i < sourceGroups.length; i++) {
            SourceGroup sourceGroup = sourceGroups[i];
            result.add(FileUtil.toFile(sourceGroup.getRootFolder()));
        }
        return result;
    }

    public static int getCurrentTimestamp() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static String fileGetContent(String path) {
        File fileToRead = new File(path);

        String content = null;
        try (FileReader fileStream = new FileReader(fileToRead);
                BufferedReader bufferedReader = new BufferedReader(fileStream)) {

            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                if (content == null) {
                    content = "";
                }
                content += line + "\n";
            }

        } catch (FileNotFoundException ex) {
            //exception Handling
        } catch (IOException ex) {
            //exception Handling
        }
        return content;
    }

    public SymfonyCompletionKeyListener kl;

    
    public String projectRootPath = "";
    public String appDevDebugProjectContainerContent = "";
    public String mayDevPHPFilePath = "";
    
    @Override
    public CompletionTask createTask(int queryType, JTextComponent jtc) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        kl = new SymfonyCompletionKeyListener();
        jtc.addKeyListener(kl);

        ArrayList<File> rootFiles = new ArrayList<File>();
        try {
            Project openProjects[] = OpenProjects.getDefault().openProjects().get();
            if (openProjects.length > 0) {
                for (int i = 0; i < openProjects.length; i++) {
                    Project project = openProjects[i];
                    ArrayList<File> proejctRootFiles = getSourceFolders(project);
                    rootFiles.addAll(proejctRootFiles);
                }
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        projectRootPath = "";
        appDevDebugProjectContainerContent = "";
        mayDevPHPFilePath = "";
        for (int i = 0; i < rootFiles.size(); i++) {
            File rootFile = rootFiles.get(i);
            String path = rootFile.getPath();
            mayDevPHPFilePath = path + File.separator + "var" + File.separator + "cache" + File.separator + "dev" + File.separator + "appDevDebugProjectContainer.php";
            projectRootPath = path;
            File mayDevPHPFile = new File(mayDevPHPFilePath);
            if (mayDevPHPFile.exists()) {
                appDevDebugProjectContainerContent = fileGetContent(mayDevPHPFilePath);
                if (appDevDebugProjectContainerContent == null) {
                    appDevDebugProjectContainerContent = "";
                }
            }
            if (appDevDebugProjectContainerContent.length() > 0) {
                break;
            }
        }
        final String tmpAppDevDebugProjectContainerContent = appDevDebugProjectContainerContent;

        ArrayList<String> bundleEntityListFromCacheData = new ArrayList<String>();

        if ((projectRootPath.length() > 0)&&(appDevDebugProjectContainerContent.length() > 0)) {
            String cacheDataPath = projectRootPath + File.separator + "var" + File.separator + "cache" + File.separator + "dev" + File.separator + "netbeanSymfonyAutoCompletePluginCacheData";
            String timestampPath = projectRootPath + File.separator + "var" + File.separator + "cache" + File.separator + "dev" + File.separator + "netbeanSymfonyAutoCompletePluginCacheTime";

            try {
                FileInputStream fis = new FileInputStream(cacheDataPath);
                ObjectInputStream ois = new ObjectInputStream(fis);
                bundleEntityListFromCacheData = (ArrayList) ois.readObject();
                ois.close();
                fis.close();
            } catch (IOException ioe) {
                bundleEntityListFromCacheData = new ArrayList<String>();
            } catch (ClassNotFoundException c) {
                bundleEntityListFromCacheData = new ArrayList<String>();
            }

            int timestampCacheData = -1;
            try {
                FileInputStream fis = new FileInputStream(timestampPath);
                ObjectInputStream ois = new ObjectInputStream(fis);
                timestampCacheData = (int) ois.readObject();
                ois.close();
                fis.close();
            } catch (IOException ioe) {
                timestampCacheData = -1;
            } catch (ClassNotFoundException c) {
                timestampCacheData = -1;
            }

            boolean doReadDirectory = true;
            if ((timestampCacheData > 0) && (bundleEntityListFromCacheData.size() > 0)) {
                if ((getCurrentTimestamp() - timestampCacheData) < 60 * 60) {
                    doReadDirectory = false;
                }
            }

            if (doReadDirectory) {
                final ArrayList<String> bundleEntityListRealTimeData = new ArrayList<String>();
                try {

                    Files.walkFileTree(Paths.get(projectRootPath), new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult visitFile(Path path,
                                BasicFileAttributes attrs) throws IOException {
                            String sPath = path.toString();
                            if (sPath.contains("Bundle" + File.separator + "Entity" + File.separator) && sPath.contains(".php")) {
                                int posBundle = sPath.indexOf("Bundle" + File.separator);
                                int posBundleName = sPath.lastIndexOf(File.separator, posBundle);
                                String bundleName = "";
                                if ((posBundle >= 0) && (posBundleName >= 0)) {
                                    bundleName = sPath.substring(posBundleName + 1, posBundle);
                                }

                                int posDotPhp = sPath.indexOf(".php");
                                int posEntityName = sPath.lastIndexOf(File.separator);
                                String entityName = "";
                                if ((posDotPhp >= 0) && (posEntityName >= 0)) {
                                    entityName = sPath.substring(posEntityName + 1, posDotPhp);
                                }
                                if ((bundleName.length() > 0) && (entityName.length() > 0)) {
                                    bundleEntityListRealTimeData.add(bundleName + "Bundle:" + entityName);
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc)
                                throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

                if (bundleEntityListRealTimeData.size() > 0) {
                    bundleEntityListFromCacheData = bundleEntityListRealTimeData;
                }
            }

            if (doReadDirectory && (bundleEntityListFromCacheData.size() > 0)) {
                try {
                    FileOutputStream fos = new FileOutputStream(cacheDataPath);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(bundleEntityListFromCacheData);
                    oos.close();
                    fos.close();
                } catch (IOException ex) {
                    //not symfony project do nothing
                }
                
                try {
                    int timestamp = getCurrentTimestamp();
                    FileOutputStream fos = new FileOutputStream(timestampPath);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(timestamp);
                    oos.close();
                    fos.close();
                } catch (IOException ex) {
                    //not symfony project do nothing
                }
            }
        }

        final ArrayList<String> bundleEntityList = bundleEntityListFromCacheData;
        
        
        class ServiceNameTypeTuple {
            public String serviceName;
            public String serviceType;
            ServiceNameTypeTuple(String serviceName, String serviceType) {
                this.serviceName = serviceName;
                this.serviceType = serviceType;
            }
        }
        ArrayList<ServiceNameTypeTuple> serviceNameTypeTupleList = new ArrayList<ServiceNameTypeTuple>();
        
        String cacheServiceNameTypeTupleDataPath = projectRootPath + File.separator + "var" + File.separator + "cache" + File.separator + "dev" + File.separator + "netbeanSymfonyAutoCompletePluginCacheServiceNameTypeTuple";
        String timestampServiceNameTypeTuplePath = projectRootPath + File.separator + "var" + File.separator + "cache" + File.separator + "dev" + File.separator + "netbeanSymfonyAutoCompletePluginCacheServiceNameTypeTime";
        try {
            FileInputStream fis = new FileInputStream(cacheServiceNameTypeTupleDataPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            serviceNameTypeTupleList = (ArrayList<ServiceNameTypeTuple>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            serviceNameTypeTupleList = new ArrayList<ServiceNameTypeTuple>();
        } catch (ClassNotFoundException c) {
            serviceNameTypeTupleList = new ArrayList<ServiceNameTypeTuple>();
        }

        int timestampCacheServiceNameTypeTuple = -1;
        try {
            FileInputStream fis = new FileInputStream(timestampServiceNameTypeTuplePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            timestampCacheServiceNameTypeTuple = (int) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            timestampCacheServiceNameTypeTuple = -1;
        } catch (ClassNotFoundException c) {
            timestampCacheServiceNameTypeTuple = -1;
        }

        boolean doConstructServiceNameTypeTupleList = true;
        if ((timestampCacheServiceNameTypeTuple > 0) && (serviceNameTypeTupleList.size() > 0)) {
            if ((getCurrentTimestamp() - timestampCacheServiceNameTypeTuple) < 60 * 60) {
                doConstructServiceNameTypeTupleList = false;
            }
        }
        
        if (doConstructServiceNameTypeTupleList && (appDevDebugProjectContainerContent.length() > 0)) {
            //提取 简单 service
            int startIndex = 0;
            int tokenRightPos;
            String serviceName = "";
            while (true) {
                serviceName = "";

                String tokenLeft = "return $this->services['";
                int tokenLeftPos = appDevDebugProjectContainerContent.indexOf(tokenLeft, startIndex);
                if (tokenLeftPos > 0) {

                    String tokenRight = "']";
                    tokenRightPos = appDevDebugProjectContainerContent.indexOf(tokenRight, tokenLeftPos + tokenLeft.length());
                    if (tokenRightPos > 0) {
                        serviceName = appDevDebugProjectContainerContent.substring(tokenLeftPos + tokenLeft.length(), tokenRightPos);
                    } else {
                        break;
                    }

                    startIndex = tokenRightPos;
                } else {
                    break;
                }

                String serviceType = "";
                if (serviceName.length() > 0) {
                    String tokenAtReturn = "@return";
                    int atReturnPos = appDevDebugProjectContainerContent.lastIndexOf(tokenAtReturn, tokenLeftPos);
                    if (atReturnPos > 0) {
                        String tokenNewLine = "\n";
                        int newLinePos = appDevDebugProjectContainerContent.indexOf(tokenNewLine, atReturnPos);
                        if (newLinePos > 0) {
                            serviceType = appDevDebugProjectContainerContent.substring(atReturnPos + tokenAtReturn.length(), newLinePos);
                        }
                    }

                }
                if (!serviceName.equals("")) {
                    serviceNameTypeTupleList.add(new ServiceNameTypeTuple(serviceName, serviceType));
                }
            }

            //提取 运行时构建的复杂service
            startIndex = 0;
            int tokenLeft2Pos;
            int tokenRight2Pos;
            while (true) {
                serviceName = "";
                String tokenLeft2 = "$this->services['";
                String tokenRight2 = "'] = $instance = ";
                tokenRight2Pos = appDevDebugProjectContainerContent.indexOf(tokenRight2, startIndex);
                if (tokenRight2Pos > 0) {
                    startIndex = tokenRight2Pos + tokenRight2.length();
                    tokenLeft2Pos = appDevDebugProjectContainerContent.lastIndexOf(tokenLeft2, tokenRight2Pos);
                    if (tokenLeft2Pos > 0) {
                        serviceName = appDevDebugProjectContainerContent.substring(tokenLeft2Pos + tokenLeft2.length(), tokenRight2Pos);
                    } else {
                        break;
                    }
                } else {
                    break;
                }

                String serviceType = "";
                if (serviceName.length() > 0) {
                    String tokenAtReturn = "@return";
                    int atReturnPos = appDevDebugProjectContainerContent.lastIndexOf(tokenAtReturn, tokenLeft2Pos);
                    if (atReturnPos > 0) {
                        String tokenNewLine = "\n";
                        int newLinePos = appDevDebugProjectContainerContent.indexOf(tokenNewLine, atReturnPos);
                        if (newLinePos > 0) {
                            serviceType = appDevDebugProjectContainerContent.substring(atReturnPos + tokenAtReturn.length(), newLinePos);
                        }
                    }
                }
                if (!serviceName.equals("")) {
                    serviceNameTypeTupleList.add(new ServiceNameTypeTuple(serviceName, serviceType));
                }
            }

            //提取参数
            startIndex = appDevDebugProjectContainerContent.indexOf("return array(", 0);
            int tokenLeft3Pos;
            int tokenRight3Pos;
            while (true) {
                serviceName = "";
                String tokenLeft3 = "'";
                String tokenRight3 = "' => '";

                tokenRight3Pos = appDevDebugProjectContainerContent.indexOf(tokenRight3, startIndex);
                if (tokenRight3Pos > 0) {
                    startIndex = tokenRight3Pos + tokenRight3.length();
                    tokenLeft3Pos = appDevDebugProjectContainerContent.lastIndexOf(tokenLeft3, tokenRight3Pos - 1);
                    if (tokenLeft3Pos > 0) {
                        serviceName = appDevDebugProjectContainerContent.substring(tokenLeft3Pos + tokenLeft3.length(), tokenRight3Pos);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
                if (!serviceName.equals("")) {
                    serviceNameTypeTupleList.add(new ServiceNameTypeTuple(serviceName, SymfonyCompletionItem.SYMFONY_PARAMETER));
                }
            }

        }
        
        if (doConstructServiceNameTypeTupleList && (serviceNameTypeTupleList.size()> 0)) {
            try {
                FileOutputStream fos = new FileOutputStream(cacheServiceNameTypeTupleDataPath);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(serviceNameTypeTupleList);
                oos.close();
                fos.close();
            } catch (IOException ex) {
                //not symfony project do nothing
            }
            
            try {
                int timestamp = getCurrentTimestamp();
                FileOutputStream fos = new FileOutputStream(timestampServiceNameTypeTuplePath);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(timestamp);
                oos.close();
                fos.close();
            } catch (IOException ex) {
                //not symfony project do nothing
            }
        }
        
        final ArrayList<ServiceNameTypeTuple> finalServiceNameTypeTupleList = serviceNameTypeTupleList;
        return new AsyncCompletionTask(new AsyncCompletionQuery() {

            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {

                String filter = null;
                int startOffset = caretOffset - 1;

                try {
                    final StyledDocument bDoc = (StyledDocument) document;
                    final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
                    final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                    final int whiteOrQuotationOffset = indexOfWhiteOrMeaningless(line);
                    filter = new String(line, whiteOrQuotationOffset + 1, line.length - whiteOrQuotationOffset - 1);
                    if (whiteOrQuotationOffset > 0) {
                        startOffset = lineStartOffset + whiteOrQuotationOffset + 1;
                    } else {
                        startOffset = lineStartOffset;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                if (finalServiceNameTypeTupleList.size() > 0) {
                    for (int i = 0; i < finalServiceNameTypeTupleList.size(); i++) {
                        ServiceNameTypeTuple snt = finalServiceNameTypeTupleList.get(i);
                        if (!snt.serviceName.equals("") && snt.serviceName.contains(filter)) {
                            completionResultSet.addItem(new SymfonyCompletionItem(startOffset, caretOffset, snt.serviceName, snt.serviceType, kl));
                        }
                    }
                }

                if (bundleEntityList.size() > 0) {
                    for (int i = 0; i < bundleEntityList.size(); i++) {
                        String bundleEntityName = bundleEntityList.get(i);
                        if (!bundleEntityName.equals("") && bundleEntityName.contains(filter)) {
                            completionResultSet.addItem(
                                    new SymfonyCompletionItem(startOffset, caretOffset, bundleEntityName, SymfonyCompletionItem.SYMFONY_BUNDLE_ENTITY, kl)
                            );
                        }
                    }
                }

                completionResultSet.finish();

            }
        }, jtc);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent jtc, String string) {
        return 0;
    }

    static int getRowFirstNonWhite(StyledDocument doc, int offset)
            throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException) new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1)
                        + ") on doc of length: " + doc.getLength(), start
                ).initCause(ex);
            }
            start++;
        }
        return start;
    }

    static int indexOfWhiteOrMeaningless(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if ((c == '"') || (c == '\'') || (c == '@') || (c == '%') || (c == ';') || (c == '!') || (c == '#') || (c == '$') || (c == '&') || (c == '~') || (c == '+') || (c == '-') || (c == '(') || (c == ')') || (c == '^') || (c == '*')) {
                return i;
            }
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }
}
