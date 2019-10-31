package org.humana.mobile.tta.data.model;

public class UpdateResponse {

        public String version_name;
        public Long version_code;
        public String status;

        public String type;

        public String getRelease_note() {
        if(release_note.isEmpty())
            return "";
        return release_note;
    }

        public String release_note;

        public Long getVersion_code() {

        return version_code;
    }

        public String getVersion_name() {
        if(version_name.isEmpty())
            return "";
        return version_name;
    }

        public String getType() {
        if(type.isEmpty())
            return "";
        return type;
    }

        public String getStatus() {
        if(status.isEmpty())
            return "";
        return status;
    }

}
