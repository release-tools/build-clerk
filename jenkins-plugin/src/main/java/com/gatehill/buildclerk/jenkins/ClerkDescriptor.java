package com.gatehill.buildclerk.jenkins;

import hudson.model.ModelObject;
import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

public interface ClerkDescriptor extends ModelObject {
    /**
     * Performs on-the-fly validation of the form field 'serverUrl'.
     *
     * @param value This parameter receives the value that the user has typed.
     * @return Indicates the outcome of the validation. This is sent to the browser.
     * <p/>
     * Note that returning {@link FormValidation#error(String)} does not
     * prevent the form from being saved. It just means that a message
     * will be displayed to the user.
     */
    default FormValidation doCheckServerUrl(@QueryParameter String value) {
        if (value.length() == 0) {
            return FormValidation.error("Please set a Server URL");
        } else if (!(value.startsWith("http://") || value.startsWith("https://"))) {
            return FormValidation.warning("Server URL should start with http:// or https://");
        }
        return FormValidation.ok();
    }
}
