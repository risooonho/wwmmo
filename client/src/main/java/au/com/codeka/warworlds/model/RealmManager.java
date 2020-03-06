package au.com.codeka.warworlds.model;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;

import au.com.codeka.warworlds.GlobalOptions;
import au.com.codeka.warworlds.RealmContext;
import au.com.codeka.warworlds.Util;

public class RealmManager {
  public static RealmManager i = new RealmManager();

  private List<Realm> realms;
  private final ArrayList<RealmChangedHandler> realmChangedHandlers;

  // The IDs for the realms can NEVER change, once set
  public static final int DEBUG_REALM_ID = 1000;
  //public static final int ALPHA_REALM_ID = 1;
  public static final int BETA_REALM_ID = 2;
  public static final int DEF_REALM_ID = 3;
  public static final int BLITZ_REALM_ID = 10;

  private RealmManager() {
    realmChangedHandlers = new ArrayList<>();

    realms = new ArrayList<>();
    try {
      if (Util.isDebug()) {
        realms.add(new Realm(DEBUG_REALM_ID, "http://127.0.0.1:8080/realms/beta/",
            "Debug",
            "The debug realm runs on my local dev box for testing."));
      }
      realms.add(new Realm(DEF_REALM_ID, "https://game.war-worlds.com/realms/def/",
          "Default",
          "The default realm. If you're new to War Worlds, you should join this realm."));
      realms.add(new Realm(BETA_REALM_ID, "https://game.war-worlds.com/realms/beta/",
          "Beta",
          "The old beta realm. Currently down, please choose Default."));
      if (Util.isDebug()) {
        realms.add(new Realm(BLITZ_REALM_ID, "https://game.war-worlds.com/realms/blitz/",
            "Blitz",
            "The goal of Blitz is to build as big an empire as you can in 1 month. Each month, the universe is reset and the winner is the one with the highest total population."));
      }
    } catch (URISyntaxException e) {
      // should never happen
    }
  }

  public void setup() {
    SharedPreferences prefs = Util.getSharedPreferences();
    if (prefs.getString("RealmName", null) != null) {
      selectRealm(prefs.getString("RealmName", null), false);
    }
  }

  public void addRealmChangedHandler(RealmChangedHandler handler) {
    synchronized (realmChangedHandlers) {
      realmChangedHandlers.add(handler);
    }
  }

  public void removeRealmChangedHandler(RealmChangedHandler handler) {
    synchronized (realmChangedHandlers) {
      realmChangedHandlers.remove(handler);
    }
  }

  protected void fireRealmChangedHandler(Realm newRealm) {
    synchronized (realmChangedHandlers) {
      for (RealmChangedHandler handler : realmChangedHandlers) {
        handler.onRealmChanged(newRealm);
      }
    }
  }

  public List<Realm> getRealms() {
    return realms;
  }

  public Realm getRealmByName(String name) {
    for (Realm realm : realms) {
      if (realm.getDisplayName().equalsIgnoreCase(name)) {
        return realm;
      }
    }
    return null;
  }

  public void selectRealm(String realmName) {
    selectRealm(realmName, true);
  }

  public void selectRealm(int realmID) {
    selectRealm(realmID, true);
  }

  private void selectRealm(String realmName, boolean saveSelection) {
    for (Realm realm : realms) {
      if (realm.getDisplayName().equalsIgnoreCase(realmName)) {
        selectRealm(realm.getID(), saveSelection);
        return;
      }
    }

    selectRealm(0, saveSelection);
  }

  private void selectRealm(int realmID, boolean saveSelection) {
    Realm currentRealm = null;
    if (realmID <= 0) {
      RealmContext.i.setGlobalRealm(null);
    } else {
      for (Realm realm : realms) {
        if (realm.getID() == realmID) {
          currentRealm = realm;
          realm.getAuthenticator().logout();
          RealmContext.i.setGlobalRealm(realm);
        }
      }
    }

    if (saveSelection) {
      Util.getSharedPreferences().edit()
          .putString("RealmName", currentRealm == null ? null : currentRealm.getDisplayName())
          .apply();
    }

    fireRealmChangedHandler(currentRealm);
  }

  public interface RealmChangedHandler {
    public void onRealmChanged(Realm newRealm);
  }
}