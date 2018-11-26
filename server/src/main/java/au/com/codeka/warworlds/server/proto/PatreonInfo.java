// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: au/com/codeka/warworlds/server/proto/patreon.proto at 4:1
package au.com.codeka.warworlds.server.proto;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * Details about a user's patreon account.
 */
public final class PatreonInfo extends Message<PatreonInfo, PatreonInfo.Builder> {
  public static final ProtoAdapter<PatreonInfo> ADAPTER = new ProtoAdapter_PatreonInfo();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_EMPIRE_ID = 0L;

  public static final String DEFAULT_ACCESS_TOKEN = "";

  public static final String DEFAULT_REFRESH_TOKEN = "";

  public static final String DEFAULT_TOKEN_TYPE = "";

  public static final String DEFAULT_TOKEN_SCOPE = "";

  public static final Long DEFAULT_TOKEN_EXPIRY_TIME = 0L;

  public static final String DEFAULT_PATREON_URL = "";

  public static final String DEFAULT_FULL_NAME = "";

  public static final String DEFAULT_DISCORD_ID = "";

  public static final String DEFAULT_ABOUT = "";

  public static final String DEFAULT_IMAGE_URL = "";

  public static final String DEFAULT_EMAIL = "";

  public static final Integer DEFAULT_MAX_PLEDGE = 0;

  /**
   * The empire_id that they associated with.
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#INT64"
  )
  public final Long empire_id;

  /**
   * OAuth access token.
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String access_token;

  /**
   * OAuth refresh token.
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String refresh_token;

  /**
   * OAuth token type.
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String token_type;

  /**
   * OAuth token_scope.
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String token_scope;

  /**
   * OAuth token expiry.
   */
  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#INT64"
  )
  public final Long token_expiry_time;

  /**
   * The URL of user's patreon page.
   */
  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String patreon_url;

  /**
   * Full name on Patreon.
   */
  @WireField(
      tag = 8,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String full_name;

  /**
   * Discord ID that they've associated with Patreon.
   */
  @WireField(
      tag = 9,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String discord_id;

  /**
   * The user's 'about' string.
   */
  @WireField(
      tag = 10,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String about;

  /**
   * The user's image.
   */
  @WireField(
      tag = 11,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String image_url;

  /**
   * The user's email address (if they have one).
   */
  @WireField(
      tag = 12,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String email;

  /**
   * The maximum current pledge amount the user has pledged to us (in cents).
   */
  @WireField(
      tag = 13,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  public final Integer max_pledge;

  public PatreonInfo(Long empire_id, String access_token, String refresh_token, String token_type, String token_scope, Long token_expiry_time, String patreon_url, String full_name, String discord_id, String about, String image_url, String email, Integer max_pledge) {
    this(empire_id, access_token, refresh_token, token_type, token_scope, token_expiry_time, patreon_url, full_name, discord_id, about, image_url, email, max_pledge, ByteString.EMPTY);
  }

  public PatreonInfo(Long empire_id, String access_token, String refresh_token, String token_type, String token_scope, Long token_expiry_time, String patreon_url, String full_name, String discord_id, String about, String image_url, String email, Integer max_pledge, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.empire_id = empire_id;
    this.access_token = access_token;
    this.refresh_token = refresh_token;
    this.token_type = token_type;
    this.token_scope = token_scope;
    this.token_expiry_time = token_expiry_time;
    this.patreon_url = patreon_url;
    this.full_name = full_name;
    this.discord_id = discord_id;
    this.about = about;
    this.image_url = image_url;
    this.email = email;
    this.max_pledge = max_pledge;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.empire_id = empire_id;
    builder.access_token = access_token;
    builder.refresh_token = refresh_token;
    builder.token_type = token_type;
    builder.token_scope = token_scope;
    builder.token_expiry_time = token_expiry_time;
    builder.patreon_url = patreon_url;
    builder.full_name = full_name;
    builder.discord_id = discord_id;
    builder.about = about;
    builder.image_url = image_url;
    builder.email = email;
    builder.max_pledge = max_pledge;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PatreonInfo)) return false;
    PatreonInfo o = (PatreonInfo) other;
    return Internal.equals(unknownFields(), o.unknownFields())
        && Internal.equals(empire_id, o.empire_id)
        && Internal.equals(access_token, o.access_token)
        && Internal.equals(refresh_token, o.refresh_token)
        && Internal.equals(token_type, o.token_type)
        && Internal.equals(token_scope, o.token_scope)
        && Internal.equals(token_expiry_time, o.token_expiry_time)
        && Internal.equals(patreon_url, o.patreon_url)
        && Internal.equals(full_name, o.full_name)
        && Internal.equals(discord_id, o.discord_id)
        && Internal.equals(about, o.about)
        && Internal.equals(image_url, o.image_url)
        && Internal.equals(email, o.email)
        && Internal.equals(max_pledge, o.max_pledge);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (empire_id != null ? empire_id.hashCode() : 0);
      result = result * 37 + (access_token != null ? access_token.hashCode() : 0);
      result = result * 37 + (refresh_token != null ? refresh_token.hashCode() : 0);
      result = result * 37 + (token_type != null ? token_type.hashCode() : 0);
      result = result * 37 + (token_scope != null ? token_scope.hashCode() : 0);
      result = result * 37 + (token_expiry_time != null ? token_expiry_time.hashCode() : 0);
      result = result * 37 + (patreon_url != null ? patreon_url.hashCode() : 0);
      result = result * 37 + (full_name != null ? full_name.hashCode() : 0);
      result = result * 37 + (discord_id != null ? discord_id.hashCode() : 0);
      result = result * 37 + (about != null ? about.hashCode() : 0);
      result = result * 37 + (image_url != null ? image_url.hashCode() : 0);
      result = result * 37 + (email != null ? email.hashCode() : 0);
      result = result * 37 + (max_pledge != null ? max_pledge.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (empire_id != null) builder.append(", empire_id=").append(empire_id);
    if (access_token != null) builder.append(", access_token=").append(access_token);
    if (refresh_token != null) builder.append(", refresh_token=").append(refresh_token);
    if (token_type != null) builder.append(", token_type=").append(token_type);
    if (token_scope != null) builder.append(", token_scope=").append(token_scope);
    if (token_expiry_time != null) builder.append(", token_expiry_time=").append(token_expiry_time);
    if (patreon_url != null) builder.append(", patreon_url=").append(patreon_url);
    if (full_name != null) builder.append(", full_name=").append(full_name);
    if (discord_id != null) builder.append(", discord_id=").append(discord_id);
    if (about != null) builder.append(", about=").append(about);
    if (image_url != null) builder.append(", image_url=").append(image_url);
    if (email != null) builder.append(", email=").append(email);
    if (max_pledge != null) builder.append(", max_pledge=").append(max_pledge);
    return builder.replace(0, 2, "PatreonInfo{").append('}').toString();
  }

  public static final class Builder extends Message.Builder<PatreonInfo, Builder> {
    public Long empire_id;

    public String access_token;

    public String refresh_token;

    public String token_type;

    public String token_scope;

    public Long token_expiry_time;

    public String patreon_url;

    public String full_name;

    public String discord_id;

    public String about;

    public String image_url;

    public String email;

    public Integer max_pledge;

    public Builder() {
    }

    /**
     * The empire_id that they associated with.
     */
    public Builder empire_id(Long empire_id) {
      this.empire_id = empire_id;
      return this;
    }

    /**
     * OAuth access token.
     */
    public Builder access_token(String access_token) {
      this.access_token = access_token;
      return this;
    }

    /**
     * OAuth refresh token.
     */
    public Builder refresh_token(String refresh_token) {
      this.refresh_token = refresh_token;
      return this;
    }

    /**
     * OAuth token type.
     */
    public Builder token_type(String token_type) {
      this.token_type = token_type;
      return this;
    }

    /**
     * OAuth token_scope.
     */
    public Builder token_scope(String token_scope) {
      this.token_scope = token_scope;
      return this;
    }

    /**
     * OAuth token expiry.
     */
    public Builder token_expiry_time(Long token_expiry_time) {
      this.token_expiry_time = token_expiry_time;
      return this;
    }

    /**
     * The URL of user's patreon page.
     */
    public Builder patreon_url(String patreon_url) {
      this.patreon_url = patreon_url;
      return this;
    }

    /**
     * Full name on Patreon.
     */
    public Builder full_name(String full_name) {
      this.full_name = full_name;
      return this;
    }

    /**
     * Discord ID that they've associated with Patreon.
     */
    public Builder discord_id(String discord_id) {
      this.discord_id = discord_id;
      return this;
    }

    /**
     * The user's 'about' string.
     */
    public Builder about(String about) {
      this.about = about;
      return this;
    }

    /**
     * The user's image.
     */
    public Builder image_url(String image_url) {
      this.image_url = image_url;
      return this;
    }

    /**
     * The user's email address (if they have one).
     */
    public Builder email(String email) {
      this.email = email;
      return this;
    }

    /**
     * The maximum current pledge amount the user has pledged to us (in cents).
     */
    public Builder max_pledge(Integer max_pledge) {
      this.max_pledge = max_pledge;
      return this;
    }

    @Override
    public PatreonInfo build() {
      return new PatreonInfo(empire_id, access_token, refresh_token, token_type, token_scope, token_expiry_time, patreon_url, full_name, discord_id, about, image_url, email, max_pledge, buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PatreonInfo extends ProtoAdapter<PatreonInfo> {
    ProtoAdapter_PatreonInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, PatreonInfo.class);
    }

    @Override
    public int encodedSize(PatreonInfo value) {
      return (value.empire_id != null ? ProtoAdapter.INT64.encodedSizeWithTag(1, value.empire_id) : 0)
          + (value.access_token != null ? ProtoAdapter.STRING.encodedSizeWithTag(2, value.access_token) : 0)
          + (value.refresh_token != null ? ProtoAdapter.STRING.encodedSizeWithTag(3, value.refresh_token) : 0)
          + (value.token_type != null ? ProtoAdapter.STRING.encodedSizeWithTag(4, value.token_type) : 0)
          + (value.token_scope != null ? ProtoAdapter.STRING.encodedSizeWithTag(5, value.token_scope) : 0)
          + (value.token_expiry_time != null ? ProtoAdapter.INT64.encodedSizeWithTag(6, value.token_expiry_time) : 0)
          + (value.patreon_url != null ? ProtoAdapter.STRING.encodedSizeWithTag(7, value.patreon_url) : 0)
          + (value.full_name != null ? ProtoAdapter.STRING.encodedSizeWithTag(8, value.full_name) : 0)
          + (value.discord_id != null ? ProtoAdapter.STRING.encodedSizeWithTag(9, value.discord_id) : 0)
          + (value.about != null ? ProtoAdapter.STRING.encodedSizeWithTag(10, value.about) : 0)
          + (value.image_url != null ? ProtoAdapter.STRING.encodedSizeWithTag(11, value.image_url) : 0)
          + (value.email != null ? ProtoAdapter.STRING.encodedSizeWithTag(12, value.email) : 0)
          + (value.max_pledge != null ? ProtoAdapter.INT32.encodedSizeWithTag(13, value.max_pledge) : 0)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PatreonInfo value) throws IOException {
      if (value.empire_id != null) ProtoAdapter.INT64.encodeWithTag(writer, 1, value.empire_id);
      if (value.access_token != null) ProtoAdapter.STRING.encodeWithTag(writer, 2, value.access_token);
      if (value.refresh_token != null) ProtoAdapter.STRING.encodeWithTag(writer, 3, value.refresh_token);
      if (value.token_type != null) ProtoAdapter.STRING.encodeWithTag(writer, 4, value.token_type);
      if (value.token_scope != null) ProtoAdapter.STRING.encodeWithTag(writer, 5, value.token_scope);
      if (value.token_expiry_time != null) ProtoAdapter.INT64.encodeWithTag(writer, 6, value.token_expiry_time);
      if (value.patreon_url != null) ProtoAdapter.STRING.encodeWithTag(writer, 7, value.patreon_url);
      if (value.full_name != null) ProtoAdapter.STRING.encodeWithTag(writer, 8, value.full_name);
      if (value.discord_id != null) ProtoAdapter.STRING.encodeWithTag(writer, 9, value.discord_id);
      if (value.about != null) ProtoAdapter.STRING.encodeWithTag(writer, 10, value.about);
      if (value.image_url != null) ProtoAdapter.STRING.encodeWithTag(writer, 11, value.image_url);
      if (value.email != null) ProtoAdapter.STRING.encodeWithTag(writer, 12, value.email);
      if (value.max_pledge != null) ProtoAdapter.INT32.encodeWithTag(writer, 13, value.max_pledge);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PatreonInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.empire_id(ProtoAdapter.INT64.decode(reader)); break;
          case 2: builder.access_token(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.refresh_token(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.token_type(ProtoAdapter.STRING.decode(reader)); break;
          case 5: builder.token_scope(ProtoAdapter.STRING.decode(reader)); break;
          case 6: builder.token_expiry_time(ProtoAdapter.INT64.decode(reader)); break;
          case 7: builder.patreon_url(ProtoAdapter.STRING.decode(reader)); break;
          case 8: builder.full_name(ProtoAdapter.STRING.decode(reader)); break;
          case 9: builder.discord_id(ProtoAdapter.STRING.decode(reader)); break;
          case 10: builder.about(ProtoAdapter.STRING.decode(reader)); break;
          case 11: builder.image_url(ProtoAdapter.STRING.decode(reader)); break;
          case 12: builder.email(ProtoAdapter.STRING.decode(reader)); break;
          case 13: builder.max_pledge(ProtoAdapter.INT32.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public PatreonInfo redact(PatreonInfo value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
