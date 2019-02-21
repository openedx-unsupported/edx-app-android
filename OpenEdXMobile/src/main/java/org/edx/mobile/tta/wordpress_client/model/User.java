package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import org.edx.mobile.tta.wordpress_client.util.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Arjun Singh
 *         Created on 2016/01/07.
 */
public class User extends BaseModel {

    public static final String JSON_FIELD_AVATAR_URLS = "avatar_urls";
    public static final String JSON_FIELD_CAPABILITIES = "capabilities";
    public static final String JSON_FIELD_DESCRIPTION = "description";
    public static final String JSON_FIELD_EMAIL = "email";
    public static final String JSON_FIELD_FIRST_NAME = "first_name";
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_LAST_NAME = "last_name";
    public static final String JSON_FIELD_LINK = "link";
    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_NICKNAME = "nickname";
    public static final String JSON_FIELD_REGISTERED_DATE = "registered_date";
    public static final String JSON_FIELD_ROLES = "roles";
    public static final String JSON_FIELD_SLUG = "slug";
    public static final String JSON_FIELD_URL = "url";
    public static final String JSON_FIELD_USERNAME = "username";
    public static final String JSON_FIELD_PASSWORD = "password";
    public static final String JSON_FIELD_LINKS = "_links";

    /**
     * Helper variable to quickly check if user is an admin when using the custom
     * getUserFromLogin or getUserFromEmail calls.
     */
    @SerializedName("admin")
    private boolean isAdmin;

    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * Avatar URLs for the object.
     */
    @JsonAdapter(AvatarUrlsDeserializer.class)
    @SerializedName("avatar_urls")
    private Map<String, String> avatarUrls = new HashMap<>();

    public void setAvatarUrls(Map<String, String> map) {
        avatarUrls = map;
    }

    public void addAvatarUrl(String key, String value) {
        avatarUrls.put(key, value);
    }

    public Map<String, String> getAvatarUrls() {
        return avatarUrls;
    }

    public User withAvatarUrls(Map<String, String> map) {
        setAvatarUrls(map);
        return this;
    }

    public User withAvatarUrl(String key, String value) {
        addAvatarUrl(key, value);
        return this;
    }


    /**
     * All capabilities assigned to the user.
     */
    //@SerializedName("capabilities")
    //private Object mCapabilities;


    /**
     * Description of the object.
     */
    @SerializedName("description")
    private String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public User withDescription(String description) {
        setDescription(description);
        return this;
    }

    /**
     * The email address for the object.
     */
    @SerializedName("email")
    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public User withEmail(String email) {
        setEmail(email);
        return this;
    }


    /**
     * Any extra capabilities assigned to the user.
     */
    //@SerializedName("extra_capabilities")
    //private Object mExtraCapabilities;


    /**
     * First name for the object.
     */
    @SerializedName("first_name")
    private String firstName;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public User withFirstName(String firstName) {
        setFirstName(firstName);
        return this;
    }

    /**
     * Unique identifier for the object.
     */
    @SerializedName("id")
    private long id;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public User withId(long id) {
        setId(id);
        return this;
    }

    /**
     * Last name for the object.
     */
    @SerializedName("last_name")
    private String lastName;

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public User withLastName(String lastName) {
        setLastName(lastName);
        return this;
    }

    /**
     * Author URL to the object.
     */
    @SerializedName("link")
    private String link;

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public User withLink(String link) {
        setLink(link);
        return this;
    }

    /**
     * Display name for the object.
     */
    @SerializedName("name")
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public User withName(String name) {
        setName(name);
        return this;
    }

    /**
     * The nickname for the object.
     */
    @SerializedName("nickname")
    private String nickName;

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public User withNickName(String nickName) {
        setNickName(nickName);
        return this;
    }

    /**
     * Registration date for the user.
     */
    @SerializedName("registered_date")
    private String registeredDate;

    public void setRegisteredDate(String date) {
        registeredDate = date;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public User withRegisteredDate(String date) {
        setRegisteredDate(date);
        return this;
    }

    /**
     * Roles assigned to the user.
     */
    @SerializedName("roles")
    private List<String> roles = new ArrayList<>();

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public User withRoles(List<String> roles) {
        setRoles(roles);
        return this;
    }

    /**
     * An alphanumeric identifier for the object unique to its type.
     */
    @SerializedName("slug")
    private String slug;

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSlug() {
        return slug;
    }

    public User withSlug(String slug) {
        setSlug(slug);
        return this;
    }

    /**
     * URL of the object.
     */
    @SerializedName("url")
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public User withUrl(String url) {
        setUrl(url);
        return this;
    }

    /**
     * Login name for the user.
     */
    @SerializedName("username")
    private String userName;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUsername() {
        return userName;
    }

    public User withUsername(String userName) {
        setUserName(userName);
        return this;
    }

    /**
     * Login password for the user.
     */
    @SerializedName("password")
    private String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public User withPassword(String password) {
        setPassword(password);
        return this;
    }

    /**
     * Links for this post; author, attachments, history, etc.
     */
    @JsonAdapter(LinksDeserializer.class)
    @SerializedName("_links")
    private ArrayList<Link> links = new ArrayList<>();

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public User withLinks(ArrayList<Link> links) {
        setLinks(links);
        return this;
    }

    public User() {
    }

    public User(Parcel in) {
        super(in);

        in.readMap(avatarUrls, String.class.getClassLoader());
        //in.readParcelable(mCapabilities);
        description = in.readString();
        email = in.readString();
        //in.readParcelable(mExtraCapabilities);
        firstName = in.readString();
        id = in.readLong();
        lastName = in.readString();
        link = in.readString();
        name = in.readString();
        nickName = in.readString();
        registeredDate = in.readString();
        in.readStringList(roles);
        slug = in.readString();
        url = in.readString();
        userName = in.readString();
        password = in.readString();
        in.readTypedList(links, Link.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeMap(avatarUrls);
        //dest.writeParcelable(mCapabilities, flags);
        dest.writeString(description);
        dest.writeString(email);
        //dest.writeParcelable(mExtraCapabilities, flags);
        dest.writeString(firstName);
        dest.writeLong(id);
        dest.writeString(lastName);
        dest.writeString(link);
        dest.writeString(name);
        dest.writeString(nickName);
        dest.writeString(registeredDate);
        dest.writeStringList(roles);
        dest.writeString(slug);
        dest.writeString(url);
        dest.writeString(userName);
        dest.writeString(password);
        dest.writeTypedList(links);
    }

    public static Map<String, Object> mapFromFields(User user) {
        Map<String, Object> map = new HashMap<>();

        //Validate.validateMapEntry(JSON_FIELD_AVATAR_URLS, user.getAvatarUrls(), map);
        Validate.validateMapEntry(JSON_FIELD_DESCRIPTION, user.getDescription(), map);
        Validate.validateMapEntry(JSON_FIELD_EMAIL, user.getEmail(), map);
        Validate.validateMapEntry(JSON_FIELD_FIRST_NAME, user.getFirstName(), map);
        Validate.validateMapEntry(JSON_FIELD_ID, user.getId(), map);
        Validate.validateMapEntry(JSON_FIELD_LAST_NAME, user.getLastName(), map);
        Validate.validateMapEntry(JSON_FIELD_LINK, user.getLink(), map);
        Validate.validateMapEntry(JSON_FIELD_NAME, user.getName(), map);
        Validate.validateMapEntry(JSON_FIELD_NICKNAME, user.getNickName(), map);
        Validate.validateMapEntry(JSON_FIELD_REGISTERED_DATE, user.getRegisteredDate(), map);
        Validate.validateMapListEntry(JSON_FIELD_ROLES, user.getRoles(), map);
        Validate.validateMapEntry(JSON_FIELD_SLUG, user.getSlug(), map);
        Validate.validateMapEntry(JSON_FIELD_URL, user.getUrl(), map);
        Validate.validateMapEntry(JSON_FIELD_USERNAME, user.getUsername(), map);
        Validate.validateMapEntry(JSON_FIELD_PASSWORD, user.getPassword(), map);
        //Validate.validateMapEntry(JSON_FIELD_LINKS, user.getLinks(), map);

        return map;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String toPrettyString() {
        return "User:\n" +
                "avatarUrls : " + avatarUrls + "\n"
                + "description : " + description + "\n"
                + "email : " + email + "\n"
                + "firstName : " + firstName + "\n"
                + "id : " + id + "\n"
                + "lastName : " + lastName + "\n"
                + "link : " + link + "\n" +
                "name : " + name + "\n" +
                "nickname : " + nickName + "\n" +
                "registeredDate : " + registeredDate + "\n" +
                "roles : " + roles + "\n" +
                "slug : " + slug + "\n" +
                "url : " + url + "\n" +
                "links : " + links;
    }

    @Override
    public String toString() {
        return "User{" +
                "avatarUrls=" + avatarUrls +
                ", description='" + description + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", id=" + id +
                ", lastName='" + lastName + '\'' +
                ", link='" + link + '\'' +
                ", name='" + name + '\'' +
                ", nickName='" + nickName + '\'' +
                ", registeredDate='" + registeredDate + '\'' +
                ", roles=" + roles +
                ", slug='" + slug + '\'' +
                ", url='" + url + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", links=" + links +
                '}';
    }
}
