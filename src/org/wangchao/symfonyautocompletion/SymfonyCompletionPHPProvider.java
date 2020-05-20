/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wangchao.symfonyautocompletion;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
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
import java.io.IOException;
import java.io.InputStreamReader;
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

    public SymfonyCompletionKeyListener kl;
    @Override
    public CompletionTask createTask(int queryType, JTextComponent jtc) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }

        kl = new SymfonyCompletionKeyListener();
        jtc.addKeyListener(kl);
        
        return new AsyncCompletionTask(new AsyncCompletionQuery() {

            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {

                String filter = null;
                int startOffset = caretOffset - 1;

                try {
                    final StyledDocument bDoc = (StyledDocument) document;
                    final int lineStartOffset = getRowFirstNonWhite(bDoc, caretOffset);
                    final char[] line = bDoc.getText(lineStartOffset, caretOffset - lineStartOffset).toCharArray();
                    final int whiteOrQuotationOffset = indexOfWhiteOrQuotation(line);
                    filter = new String(line, whiteOrQuotationOffset + 1, line.length - whiteOrQuotationOffset - 1);
                    if (whiteOrQuotationOffset > 0) {
                        startOffset = lineStartOffset + whiteOrQuotationOffset + 1;
                    } else {
                        startOffset = lineStartOffset;
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                //获取当前symfony项目下的service别名
                //获取/var/cache/dev/appDevDebugProjectContainer.php路径
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

                String appDevDebugProjectContainerPath = "";
                String appDevDebugProjectContainerContent = "";
                for (int i = 0; i < rootFiles.size(); i++) {
                    File rootFile = rootFiles.get(i);
                    String path = rootFile.getPath();
                    String mayDevPHPFilePath = path + File.separator + "var" + File.separator + "cache" + File.separator + "dev" + File.separator + "appDevDebugProjectContainer.php";
                    File mayDevPHPFile = new File(mayDevPHPFilePath);
                    FileInputStream is = null;
                    StringBuilder stringBuilder = null;
                    if (mayDevPHPFile.exists()) {
                        try {
                            is = new FileInputStream(mayDevPHPFile);
                            InputStreamReader streamReader = new InputStreamReader(is);
                            BufferedReader reader = new BufferedReader(streamReader);
                            String line;
                            stringBuilder = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line + "\n");
                            }
                            reader.close();
                            is.close();
                        } catch (FileNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        appDevDebugProjectContainerContent = String.valueOf(stringBuilder);
                    }
                    if (appDevDebugProjectContainerContent.length() > 0) {
                        break;
                    }
                }

                if (appDevDebugProjectContainerContent.length() > 0) {
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

                        if (!serviceName.equals("") && serviceName.contains(filter)) {
                            completionResultSet.addItem(new SymfonyCompletionItem(startOffset, caretOffset, serviceName, serviceType, kl));
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
                        if (!serviceName.equals("") && serviceName.contains(filter)) {
                            completionResultSet.addItem(new SymfonyCompletionItem(startOffset, caretOffset, serviceName, serviceType, kl));
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
                        if (!serviceName.equals("") && serviceName.contains(filter)) {
                            completionResultSet.addItem(new SymfonyCompletionItem(startOffset, caretOffset, serviceName, SymfonyCompletionItem.SYMFONY_PARAMETER, kl));
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

    static int indexOfWhiteOrQuotation(char[] line) {
        int i = line.length;
        while (--i > -1) {
            final char c = line[i];
            if ((c == '"') || (c == '\'')) {
                return i;
            }
            if (Character.isWhitespace(c)) {
                return i;
            }
        }
        return -1;
    }
}
