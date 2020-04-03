package au.com.codeka.warworlds;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import au.com.codeka.BackgroundRunner;
import au.com.codeka.common.Log;
import au.com.codeka.common.protobuf.Messages;
import au.com.codeka.warworlds.ctrl.TransparentWebView;
import au.com.codeka.warworlds.eventbus.EventHandler;
import au.com.codeka.warworlds.game.starfield.StarfieldActivity;
import au.com.codeka.warworlds.model.EmpireManager;
import au.com.codeka.warworlds.model.EmpireShieldManager;
import au.com.codeka.warworlds.model.MyEmpire;
import au.com.codeka.warworlds.model.ShieldManager;

/**
 * Main activity. Displays the message of the day and lets you select "Start Game", "Options", etc.
 */
public class WarWorldsActivity extends BaseActivity {
  private static final Log log = new Log("WarWorldsActivity");
  private Context context = this;
  private Button startGameButton;
  private TextView connectionStatus;
  private HelloWatcher helloWatcher;
  private TextView realmName;

  @Nullable
  private Intent startGameIntent;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    log.info("WarWorlds activity starting...");
    super.onCreate(savedInstanceState);

    setContentView(R.layout.welcome);
    Util.setup(context);

    if (onBlueStacks()) {
      Toast.makeText(
          this,
          "Sorry, this platform is not supported. Please use a supported platform.",
          Toast.LENGTH_LONG).show();
      finish();
    }

    View rootView = findViewById(android.R.id.content);
    ActivityBackgroundGenerator.setBackground(rootView);

    startGameButton = findViewById(R.id.start_game_btn);
    connectionStatus = findViewById(R.id.connection_status);
    realmName = findViewById(R.id.realm_name);
    final Button realmSelectButton = findViewById(R.id.realm_select_btn);
    final Button optionsButton = findViewById(R.id.options_btn);

    refreshWelcomeMessage();

    realmSelectButton.setOnClickListener(
        v -> startActivity(new Intent(context, RealmSelectActivity.class)));
    optionsButton.setOnClickListener(
        v -> startActivity(new Intent(context, GlobalOptionsActivity.class)));

    startGameButton.setOnClickListener(v -> {
      if (startGameIntent == null) {
        final Intent intent = new Intent(context, StarfieldActivity.class);
        startActivity(intent);
      } else {
        startActivity(startGameIntent);
      }
    });

    findViewById(R.id.help_btn).setOnClickListener(v -> {
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setData(Uri.parse("https://war-worlds.wikia.com/wiki/War_Worlds_Wiki"));
      startActivity(i);
    });

    findViewById(R.id.website_btn).setOnClickListener(v -> {
      Intent i = new Intent(Intent.ACTION_VIEW);
      i.setData(Uri.parse("http://www.war-worlds.com/"));
      startActivity(i);
    });

    findViewById(R.id.reauth_btn).setOnClickListener(v -> onReauthClick());
  }

  @Override
  public void onResumeFragments() {
    super.onResumeFragments();
    final SharedPreferences prefs = Util.getSharedPreferences();
    if (!prefs.getBoolean("WarmWelcome", false)) {
      // if we've never done the warm-welcome, do it now
      log.info("Starting Warm Welcome");
      startActivity(new Intent(this, WarmWelcomeActivity.class));
      return;
    }

    if (RealmContext.i.getCurrentRealm() == null) {
      log.info("No realm selected, switching to RealmSelectActivity");
      startActivity(new Intent(this, RealmSelectActivity.class));
      return;
    }

    startGameButton.setEnabled(false);
    realmName.setText(String
        .format(Locale.ENGLISH, "Realm: %s", RealmContext.i.getCurrentRealm().getDisplayName()));

    final TextView empireName = findViewById(R.id.empire_name);
    final ImageView empireIcon = findViewById(R.id.empire_icon);
    empireName.setText("");
    empireIcon.setImageBitmap(null);

    helloWatcher = new HelloWatcher();
    ServerGreeter.addHelloWatcher(helloWatcher);

    ShieldManager.eventBus.register(eventHandler);

    ServerGreeter.waitForHello(this, (success, greeting) -> {
      if (success) {
        // we'll display a bit of debugging info along with the 'connected' message
        long maxMemoryBytes = Runtime.getRuntime().maxMemory();
        int memoryClass = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryClass();

        String serverVersion = "?";
        Messages.HelloResponse helloResponse = ServerGreeter.getHelloResponse();
        if (helloResponse != null) {
          serverVersion = helloResponse.getServerVersion();
        }
        DecimalFormat formatter = new DecimalFormat("#,##0");
        String msg = String.format(Locale.ENGLISH,
            "Connected\r\nMemory Class: %d - Max bytes: %s\r\nVersion: %s%s Server: %s",
            memoryClass,
            formatter.format(maxMemoryBytes),
            Util.getVersion(),
            Util.isDebug() ? " (debug)" : "",
            serverVersion);
        connectionStatus.setText(msg);
        connectionStatus.setTextColor(Color.WHITE);
        startGameButton.setEnabled(true);
        startGameIntent = null;

        MyEmpire empire = EmpireManager.i.getEmpire();
        if (empire != null) {
          empireName.setText(empire.getDisplayName());
          empireIcon.setImageBitmap(EmpireShieldManager.i.getShield(context, empire));
        }

        String currAccountName = prefs.getString("AccountName", null);
        if (currAccountName != null && currAccountName.endsWith("@anon.war-worlds.com")) {
          Button reauthButton = findViewById(R.id.reauth_btn);
          reauthButton.setText("Sign in");
        }
        maybeShowSignInPrompt();
      }
    });
  }

  private void onReauthClick() {
    final Intent intent = new Intent(context, AccountsActivity.class);
    startActivity(intent);
  }

  private void maybeShowSignInPrompt() {
    final SharedPreferences prefs = Util.getSharedPreferences();
    int numStartsSinceSignInPrompt = prefs.getInt("NumStartsSinceSignInPrompt", 0);
    if (numStartsSinceSignInPrompt < 5) {
      prefs.edit().putInt("NumStartsSinceSignInPrompt", numStartsSinceSignInPrompt + 1).apply();
      return;
    }

    // set the count to -95, which means they won't get prompted for another 100 starts... should
    // be plenty to not be annoying, yet still be a useful prompt.
    prefs.edit().putInt("NumStartsSinceSignInPrompt", -95).apply();
    new StyledDialog.Builder(context)
        .setMessage(Html.fromHtml("<p>In order to ensure your empire is safe in the event you lose "
            + "your phone, it's recommended that you sign in. You must also sign in if you want to "
            + "access your empire from multiple devices.</p><p>Click \"Sign in\" below to sign in "
            + "with a Google account.</p>"))
        .setTitle("Sign in")
        .setNegativeButton("No, thanks", null)
        .setPositiveButton("Sign in", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                onReauthClick();
              }
            })
        .create().show();
  }

  private void refreshWelcomeMessage() {
    new BackgroundRunner<Document>() {
      @Override
      protected Document doInBackground() {
        String url = (String) Util.getProperties().get("welcome.rss");
        try {
          // we have to use the built-in one because our special version assume all requests go
          // to the game server...
          HttpClient httpClient = new DefaultHttpClient();
          HttpGet get = new HttpGet(url);
          get.addHeader(HTTP.USER_AGENT, "wwmmo/" + Util.getVersion());
          HttpResponse response = httpClient.execute(new HttpGet(url));
          if (response.getStatusLine().getStatusCode() == 200) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);

            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            return builder.parse(response.getEntity().getContent());
          }
        } catch (Exception e) {
          log.error("Error fetching MOTD.", e);
        }

        return null;
      }

      @Override
      protected void onComplete(Document rss) {
        SimpleDateFormat inputFormat =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        SimpleDateFormat outputFormat =
            new SimpleDateFormat("dd MMM yyyy h:mm a", Locale.US);

        StringBuilder motd = new StringBuilder();
        if (rss != null) {
          NodeList itemNodes = rss.getElementsByTagName("item");
          for (int i = 0; i < itemNodes.getLength(); i++) {
            Element itemElem = (Element) itemNodes.item(i);
            String title = itemElem.getElementsByTagName("title").item(0).getTextContent();
            String content = itemElem.getElementsByTagName("description").item(0).getTextContent();
            String pubDate = itemElem.getElementsByTagName("pubDate").item(0).getTextContent();
            String link = itemElem.getElementsByTagName("link").item(0).getTextContent();

            try {
              Date date = inputFormat.parse(pubDate);
              motd.append("<h1>");
              motd.append(outputFormat.format(date));
              motd.append("</h1>");
            } catch (ParseException e) {
              // Shouldn't ever happen.
            }

            motd.append("<h2>");
            motd.append(title);
            motd.append("</h2>");
            motd.append(content);
            motd.append("<div style=\"text-align: right; border-bottom: dashed 1px #fff; "
                + "padding-bottom: 4px;\">");
            motd.append("<a href=\"");
            motd.append(link);
            motd.append("\">");
            motd.append("View forum post");
            motd.append("</a></div>");
          }
        }

        TransparentWebView motdView = findViewById(R.id.motd);
        motdView.loadHtml("html/skeleton.html", motd.toString());
      }
    }.execute();
  }

  @Override
  public void onPause() {
    super.onPause();
    ServerGreeter.removeHelloWatcher(helloWatcher);
    ShieldManager.eventBus.unregister(eventHandler);
  }

  private Object eventHandler = new Object() {
    @EventHandler
    public void onShieldUpdated(ShieldManager.ShieldUpdatedEvent event) {
      // if it's the same as our empire, we'll need to update the icon we're currently showing.
      MyEmpire empire = EmpireManager.i.getEmpire();
      if (event.id == Integer.parseInt(empire.getKey())) {
        ImageView empireIcon = findViewById(R.id.empire_icon);
        empireIcon.setImageBitmap(EmpireShieldManager.i.getShield(context, empire));
      }
    }
  };

  private class HelloWatcher implements ServerGreeter.HelloWatcher {
    private int numRetries = 0;

    @Override
    public void onRetry(final int retries) {
      numRetries = retries + 1;
      connectionStatus.setText(String.format(Locale.ENGLISH, "Retrying (#%d)...", numRetries));
      connectionStatus.setTextColor(Color.WHITE);
    }

    @Override
    public void onAuthenticating() {
      if (numRetries > 0) {
        return;
      }
      connectionStatus.setText("Authenticating...");
    }

    @Override
    public void onConnecting() {
      if (numRetries > 0) {
        return;
      }
      connectionStatus.setText("Connecting...");
    }

    @Override
    public void onFailed(String msg, ServerGreeter.GiveUpReason reason) {
      connectionStatus.setText(msg);
      connectionStatus.setTextColor(Color.RED);

      if (reason == null) {
        return;
      }

      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setPackage("com.android.vending");

      switch (reason) {
        case UPGRADE_REQUIRED:
          startGameButton.setText("Get update");
          startGameButton.setEnabled(true);

          intent.setData(Uri.parse(
              "https://play.google.com/store/apps/details?id=au.com.codeka.warworlds"));
          startGameIntent = intent;
          break;
        case GOOGLE_PLAY_SERVICES:
          startGameButton.setText("Get update");
          startGameButton.setEnabled(true);

          intent.setData(Uri.parse(
              "https://play.google.com/store/apps/details?id=com.google.android.gms"));
          startGameIntent = intent;
          break;
      }
    }
  }

  private boolean onBlueStacks() {
    File sharedFolder = new File(
        Environment.getExternalStorageDirectory(), "/windows/BstSharedFolder");
    return sharedFolder.exists();
  }
}
