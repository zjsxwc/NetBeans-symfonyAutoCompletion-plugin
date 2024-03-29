/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wangchao.symfonyautocompletion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.*;

/**
 *
 * @author wangchao
 */
public class SymfonyCompletionItem implements CompletionItem {

    public SymfonyCompletionKeyListener kl;
    public String text;
    private static ImageIcon fieldIcon
            = new ImageIcon(ImageUtilities.loadImage("org/wangchao/symfonyautocompletion/resources/icon.png"));
    private static Color fieldColor = Color.decode("0x0000B2");
    private int caretOffset;
    private int dotOffset;
    
    
    public String serviceName = "";
    public String serviceType = "";
    
    public static final String SYMFONY_PARAMETER = "SYMFONY_PARAMETER";
    public static final String SYMFONY_BUNDLE_ENTITY = "SYMFONY_BUNDLE_ENTITY";
    
    SymfonyCompletionItem(int dotOffset, int caretOffset, String serviceName, String serviceType, SymfonyCompletionKeyListener kl) {
        this.text = serviceName;
        this.dotOffset = dotOffset;
        this.caretOffset = caretOffset;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.kl = kl;
    }

    @Override
    public void defaultAction(JTextComponent jtc) {
        try {
            StyledDocument doc = (StyledDocument) jtc.getDocument();
            
            String line = "";
            int curOffset = -1;
            String curChar = "";
            while (true) {
                if ((dotOffset + curOffset) < 0) {
                    break;
                }
                curChar =  doc.getText(dotOffset + curOffset, 1);
                if ("\n".equals(curChar)) {
                    break;
                }
                line =  curChar + line;
                curOffset -= 1;
            }
            String variableName = "";
            if (line.length() > 0) {
                String[] lineSegments = line.split("=");
                if (lineSegments.length == 2) {
                    String leftPartLine = lineSegments[0];
                    String trimedLine = leftPartLine.trim();
                    if (trimedLine.length() > 0) {
                        String[] leftLineSegments = trimedLine.split("\\$");
                        if (leftLineSegments.length == 2) {
                            variableName = leftLineSegments[1];
                            variableName = variableName.trim();
                        }
                    }
                }
            }
                
            
            String dotString = doc.getText(dotOffset - 1, 1);
            doc.remove(dotOffset, caretOffset - dotOffset);
            if (dotString.equals("'") || dotString.equals("\"")) {
                doc.insertString(dotOffset, dotString, null);
                doc.remove(dotOffset, dotString.length());
            }
            doc.insertString(dotOffset, serviceName, null);
            
            if (kl.shiftKeyPressing) {
                String curServiceType = serviceType;
                if (serviceType.equals(SYMFONY_BUNDLE_ENTITY)) {
                    //change OrderBundle:UserOrder to  \OrderBundle\Repository\UserOrderRepository
                    String[] serviceNameSeg = serviceName.split(":");
                    if (serviceNameSeg.length == 2) {
                        curServiceType = "\\"+serviceNameSeg[0]+"\\Repository\\"+serviceNameSeg[1]+"Repository";
                    }
                }
                if (variableName.length() > 0) {
                    int variableNamePos = line.indexOf(variableName);
                    String whiteCharString = line.substring(0, variableNamePos - 1);
                    doc.insertString(dotOffset + curOffset + 1, whiteCharString +"/** @var " + curServiceType + " $" + variableName + " */\n", null);
                } else {
                    doc.insertString(dotOffset + serviceName.length(), " /** @var " + curServiceType + " */", null);
                }
            }
            
            Completion.get().hideAll();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent ke) {
        
    }

    @Override
    public int getPreferredWidth(Graphics graphics, Font font) {
        return CompletionUtilities.getPreferredWidth(text, null, graphics, font);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor,
            Color backgroundColor, int width, int height, boolean selected) 
    {
        String rText = text;
        if (serviceType == null ? SYMFONY_PARAMETER == null : serviceType.equals(SYMFONY_PARAMETER)) {
            rText += " ... SYMFONY_PARAMETER";
        }
        CompletionUtilities.renderHtml(fieldIcon, rText, null, g, defaultFont,
                (selected ? Color.white : fieldColor), width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int i) {
                completionResultSet.setDocumentation(new SymfonyCompletionDocumentation(SymfonyCompletionItem.this));
                completionResultSet.finish();
            }
        });
    }

    @Override
    public CompletionTask createToolTipTask() {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int i) {
                JToolTip toolTip = new JToolTip();
                toolTip.setTipText("Press Enter to insert \"" + text + "\"");
                completionResultSet.setToolTip(toolTip);
                completionResultSet.finish();
            }
        });
    }

    @Override
    public boolean instantSubstitution(JTextComponent jtc) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return text;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return text;
    }
    
}
