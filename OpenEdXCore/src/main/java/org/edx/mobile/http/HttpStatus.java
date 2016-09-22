package org.edx.mobile.http;

/**
 * A comprehensive collection of constants for HTTP status codes.
 */
public class HttpStatus {
    // Make this class non-instantiable
    private HttpStatus() {
        throw new UnsupportedOperationException();
    }

    /**
     * 100: Continue
     */
    public static final int CONTINUE = 100;

    /**
     * 101: Switching Protocols
     */
    public static final int SWITCHING_PROTOCOLS = 101;

    /**
     * 102: Processing (WebDAV, RFC2518)
     */
    public static final int PROCESSING = 102;

    /**
     * 200: OK
     */
    public static final int OK = 200;

    /**
     * 201: Created
     */
    public static final int CREATED = 201;

    /**
     * 202: Accepted
     */
    public static final int ACCEPTED = 202;

    /**
     * 203: Non-Authoritative Information (since HTTP/1.1)
     */
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;

    /**
     * 204: No Content
     */
    public static final int NO_CONTENT = 204;

    /**
     * 205: Reset Content
     */
    public static final int RESET_CONTENT = 205;

    /**
     * 206: Partial Content
     */
    public static final int PARTIAL_CONTENT = 206;

    /**
     * 207: Multi-Status (WebDAV, RFC2518)
     */
    public static final int MULTI_STATUS = 207;

    /**
     * 300: Multiple Choices
     */
    public static final int MULTIPLE_CHOICES = 300;

    /**
     * 301: Moved Permanently
     */
    public static final int MOVED_PERMANENTLY = 301;

    /**
     * 302: Found
     */
    public static final int FOUND = 302;

    /**
     * 303: See Other (since HTTP/1.1)
     */
    public static final int SEE_OTHER = 303;

    /**
     * 304: Not Modified
     */
    public static final int NOT_MODIFIED = 304;

    /**
     * 305: Use Proxy (since HTTP/1.1)
     */
    public static final int USE_PROXY = 305;

    /**
     * 307: Temporary Redirect (since HTTP/1.1)
     */
    public static final int TEMPORARY_REDIRECT = 307;

    /**
     * 400: Bad Request
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 401: Unauthorized
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 402: Payment Required
     */
    public static final int PAYMENT_REQUIRED = 402;

    /**
     * 403: Forbidden
     */
    public static final int FORBIDDEN = 403;

    /**
     * 404: Not Found
     */
    public static final int NOT_FOUND = 404;

    /**
     * 405: Method Not Allowed
     */
    public static final int METHOD_NOT_ALLOWED = 405;

    /**
     * 406: Not Acceptable
     */
    public static final int NOT_ACCEPTABLE = 406;

    /**
     * 407: Proxy Authentication Required
     */
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

    /**
     * 408: Request Timeout
     */
    public static final int REQUEST_TIMEOUT = 408;

    /**
     * 409: Conflict
     */
    public static final int CONFLICT = 409;

    /**
     * 410: Gone
     */
    public static final int GONE = 410;

    /**
     * 411: Length Required
     */
    public static final int LENGTH_REQUIRED = 411;

    /**
     * 412: Precondition Failed
     */
    public static final int PRECONDITION_FAILED = 412;

    /**
     * 413: Request Entity Too Large
     */
    public static final int REQUEST_ENTITY_TOO_LARGE = 413;

    /**
     * 414: Request-URI Too Long
     */
    public static final int REQUEST_URI_TOO_LONG = 414;

    /**
     * 415: Unsupported Media Type
     */
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * 416: Requested Range Not Satisfiable
     */
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    /**
     * 417: Expectation Failed
     */
    public static final int EXPECTATION_FAILED = 417;

    /**
     * 421: Misdirected Request
     *
     * <a href="https://tools.ietf.org/html/draft-ietf-httpbis-http2-15#section-9.1.2">421 Status Code</a>
     */
    public static final int MISDIRECTED_REQUEST = 421;

    /**
     * 422: Unprocessable Entity (WebDAV, RFC4918)
     */
    public static final int UNPROCESSABLE_ENTITY = 422;

    /**
     * 423: Locked (WebDAV, RFC4918)
     */
    public static final int LOCKED = 423;

    /**
     * 424: Failed Dependency (WebDAV, RFC4918)
     */
    public static final int FAILED_DEPENDENCY = 424;

    /**
     * 425: Unordered Collection (WebDAV, RFC3648)
     */
    public static final int UNORDERED_COLLECTION = 425;

    /**
     * 426: Upgrade Required (RFC2817)
     */
    public static final int UPGRADE_REQUIRED = 426;

    /**
     * 428: Precondition Required (RFC6585)
     */
    public static final int PRECONDITION_REQUIRED = 428;

    /**
     * 429: Too Many Requests (RFC6585)
     */
    public static final int TOO_MANY_REQUESTS = 429;

    /**
     * 431: Request Header Fields Too Large (RFC6585)
     */
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

    /**
     * 500: Internal Server Error
     */
    public static final int INTERNAL_SERVER_ERROR = 500;

    /**
     * 501: Not Implemented
     */
    public static final int NOT_IMPLEMENTED = 501;

    /**
     * 502: Bad Gateway
     */
    public static final int BAD_GATEWAY = 502;

    /**
     * 503: Service Unavailable
     */
    public static final int SERVICE_UNAVAILABLE = 503;

    /**
     * 504: Gateway Timeout
     */
    public static final int GATEWAY_TIMEOUT = 504;

    /**
     * 505: HTTP Version Not Supported
     */
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

    /**
     * 506: Variant Also Negotiates (RFC2295)
     */
    public static final int VARIANT_ALSO_NEGOTIATES = 506;

    /**
     * 507: Insufficient Storage (WebDAV, RFC4918)
     */
    public static final int INSUFFICIENT_STORAGE = 507;

    /**
     * 510: Not Extended (RFC2774)
     */
    public static final int NOT_EXTENDED = 510;

    /**
     * 511: Network Authentication Required (RFC6585)
     */
    public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;
}
