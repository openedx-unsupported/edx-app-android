package org.edx.mobile.model.registration;

import java.util.List;

public class RegistrationDescription {
    // We should use this to make the actual registration request, though it will be very surprising
    // if it’s different from the endpoint we use to GET the description.
    String endpoint;

    // Similarly, we should use this method, though it will almost certainly be always be POST.
    // If methods dynamically is a problem, it’s okay to ignore it.
    String method;
    List<RegistrationFormField> fields;
}
