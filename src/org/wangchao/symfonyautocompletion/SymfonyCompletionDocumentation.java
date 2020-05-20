/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wangchao.symfonyautocompletion;

import java.net.URL;
import javax.swing.Action;
import org.netbeans.spi.editor.completion.CompletionDocumentation;

/**
 *
 * @author wangchao
 */
public class SymfonyCompletionDocumentation implements CompletionDocumentation {

    private SymfonyCompletionItem item;

    SymfonyCompletionDocumentation(SymfonyCompletionItem item) {
        this.item = item;
    }
    
    
    @Override
    public String getText() {
        return "Information about " + item.serviceName + " :\n" + item.serviceType + "\n Also You can keep `shift` pressing to enter the service type";
    }

    @Override
    public URL getURL() {
        return null;
    }

    @Override
    public CompletionDocumentation resolveLink(String string) {
        return null;
    }

    @Override
    public Action getGotoSourceAction() {
        return null;
    }
    
}
