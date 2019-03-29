package org.edx.mobile.tta.data.enums;

public enum CertificateStatus {

    APPLICABLE,
    PROGRESS,
    GENERATED,
    NONE,
    FAIL;

    public static CertificateStatus getEnumFromString(String s){
        switch (s.toLowerCase()){
            case "applicable": return APPLICABLE;
            case "progress": return PROGRESS;
            case "generated": return GENERATED;
            case "none": return NONE;
            default: return FAIL;
        }
    }

}
