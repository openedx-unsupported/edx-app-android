package org.edx.mobile.user;

import java.util.List;

public class UserInfo {
    private String grade;
    private String url;
    private String education_board;
    private String user;
    private String user_type;

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEducation_board() {
        return education_board;
    }

    public void setEducation_board(String education_board) {
        this.education_board = education_board;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public List<PreferedLangList> getPrefered_lang_list() {
        return prefered_lang_list;
    }

    public void setPrefered_lang_list(List<PreferedLangList> prefered_lang_list) {
        this.prefered_lang_list = prefered_lang_list;
    }

    public PreferedLangList getSelected_preferd_language() {
        return selected_preferd_language;
    }

    public void setSelected_preferd_language(PreferedLangList selected_preferd_language) {
        this.selected_preferd_language = selected_preferd_language;
    }

    private String school;
    private List<PreferedLangList> prefered_lang_list;
    private PreferedLangList selected_preferd_language;
}
