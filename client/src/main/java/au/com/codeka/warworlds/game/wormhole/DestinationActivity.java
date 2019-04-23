package au.com.codeka.warworlds.game.wormhole;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import au.com.codeka.BackgroundRunner;
import au.com.codeka.common.Log;
import au.com.codeka.common.protobuf.Messages;
import au.com.codeka.warworlds.BaseActivity;
import au.com.codeka.warworlds.R;
import au.com.codeka.warworlds.api.ApiClient;
import au.com.codeka.warworlds.api.ApiException;
import au.com.codeka.warworlds.eventbus.EventHandler;
import au.com.codeka.warworlds.model.AllianceManager;
import au.com.codeka.warworlds.model.Empire;
import au.com.codeka.warworlds.model.EmpireManager;
import au.com.codeka.warworlds.model.MyEmpire;
import au.com.codeka.warworlds.model.Sprite;
import au.com.codeka.warworlds.model.SpriteDrawable;
import au.com.codeka.warworlds.model.Star;
import au.com.codeka.warworlds.model.StarImageManager;
import au.com.codeka.warworlds.model.StarManager;

import static com.google.common.base.Preconditions.checkNotNull;

public class DestinationActivity extends BaseActivity {
  private static final Log log = new Log("DestinationActivity");
  private Star srcWormhole;
  private Star destWormhole;
  private DestinationRecyclerViewHelper recyclerViewHelper;

  /** Create an {@link Intent} needed to start this activity. */
  public static Intent newStartIntent(Context context, Star srcWormhole) {
    Messages.Star.Builder starBuilder = Messages.Star.newBuilder();
    srcWormhole.toProtocolBuffer(starBuilder);

    Intent intent = new Intent(context, DestinationActivity.class);
    intent.putExtra("srcWormhole", starBuilder.build().toByteArray());
    return intent;
  }

  private Star getSrcWormhole() {
    if (srcWormhole == null) {
      Bundle args = checkNotNull(getIntent().getExtras());
      srcWormhole = new Star();
      try {
        Messages.Star starMsg = Messages.Star.parseFrom(args.getByteArray("srcWormhole"));
        srcWormhole.fromProtocolBuffer(starMsg);
      } catch (InvalidProtocolBufferException e) {
        log.error("Failed to load srcWormhole from Protocol Buffer", e);
      }
    }

    return srcWormhole;
  }

  @SuppressLint("InflateParams")
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.wormhole_destination);

    final View progressBar = findViewById(R.id.progress_bar);
    final RecyclerView wormholes = findViewById(R.id.wormholes);
    final View noWormholesMsg = findViewById(R.id.no_wormholes_msg);
    final TextView tuneTime = findViewById(R.id.tune_time);

    final MyEmpire myEmpire = EmpireManager.i.getEmpire();
    recyclerViewHelper = new DestinationRecyclerViewHelper(
        wormholes,
        getSrcWormhole(),
        new DestinationRecyclerViewHelper.Callbacks() {
      @Override
      public void onWormholeClick(Star wormhole) {

      }

      @Override
      public void fetchRows(
          final int startPosition,
          final int count,
          final DestinationRecyclerViewHelper.RowsFetchCallback callback) {
        if (myEmpire.getAlliance() != null) {
          AllianceManager.i.fetchWormholes(Integer.parseInt(myEmpire.getAlliance().getKey()),
              new AllianceManager.FetchWormholesCompleteHandler() {
                @Override
                public void onWormholesFetched(List<Star> wormholes) {
                  final View progressBar = findViewById(R.id.progress_bar);
                  final RecyclerView wormholesList = findViewById(R.id.wormholes);
                  final View noWormholesMsg = findViewById(R.id.no_wormholes_msg);

                  // Remove the current wormhole, since obviously you can't tune to that.
                  for (int i = 0; i < wormholes.size(); i++) {
                    if (wormholes.get(i).getID() == getSrcWormhole().getID()) {
                      wormholes.remove(i);
                      break;
                    }
                  }

                  progressBar.setVisibility(View.GONE);
                  if (startPosition == 0 && wormholes.isEmpty()) {
                    wormholesList.setVisibility(View.GONE);
                    noWormholesMsg.setVisibility(View.VISIBLE);
                  } else {
                    wormholesList.setVisibility(View.VISIBLE);
                    noWormholesMsg.setVisibility(View.GONE);
                    tuneTime.setVisibility(View.VISIBLE);
                  }

                  callback.onRowsFetched(wormholes);
                }
              });
        } else {
          // TODO: support wormholes in your own empire at least...
        }
      }
    });

    progressBar.setVisibility(View.VISIBLE);
    wormholes.setVisibility(View.GONE);
    tuneTime.setVisibility(View.GONE);
    noWormholesMsg.setVisibility(View.GONE);

    TextView starName = findViewById(R.id.star_name);
    starName.setText(getSrcWormhole().getName());

    ImageView starIcon = findViewById(R.id.star_icon);
    Sprite starSprite = StarImageManager.getInstance().getSprite(getSrcWormhole(), 60, true);
    starIcon.setImageDrawable(new SpriteDrawable(starSprite));

    /*
    int tuneTimeHours = getSrcWormhole().getWormholeExtra() == null ? 0 : getSrcWormhole()
        .getWormholeExtra().getTuneTimeHours();
    tuneTime.setText(String.format(Locale.ENGLISH, "Tune time: %d hr%s", tuneTimeHours,
        tuneTimeHours == 1 ? "" : "s"));
    */

    /*
    b.setPositiveButton("Tune", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface d, int id) {
        onTuneClicked();
        d.dismiss();
      }
    });
    b.setNegativeButton("Cancel", null);

    dialog = b.create();
    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
      @Override
      public void onShow(DialogInterface d) {
        dialog.getPositiveButton().setEnabled(false);
      }
    });
*/
    EmpireManager.eventBus.register(eventHandler);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    EmpireManager.eventBus.unregister(eventHandler);
  }

  private final View.OnClickListener itemClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      destWormhole = (Star) v.getTag();
      // TODO: enable 'tune' button
      //dialog.getPositiveButton().setEnabled(true);
    }
  };

  private void onTuneClicked() {
    if (destWormhole == null) {
      return;
    }

    new BackgroundRunner<Star>() {
      @Override
      protected Star doInBackground() {
        String url = "stars/" + getSrcWormhole().getKey() + "/wormhole/tune";
        try {
          Messages.WormholeTuneRequest request_pb = Messages.WormholeTuneRequest.newBuilder()
              .setSrcStarId(Integer.parseInt(getSrcWormhole().getKey()))
              .setDestStarId(Integer.parseInt(destWormhole.getKey())).build();
          Messages.Star pb = ApiClient.postProtoBuf(url, request_pb, Messages.Star.class);
          Star star = new Star();
          star.fromProtocolBuffer(pb);
          return star;
        } catch (ApiException e) {
          return null;
        }
      }

      @Override
      protected void onComplete(Star star) {
        if (star != null) {
          StarManager.i.notifyStarUpdated(star);
        }
      }
    }.execute();
  }

  private final Object eventHandler = new Object() {
    @EventHandler
    public void onEmpireUpdated(Empire empire) {/*
      final LinearLayout wormholeItemContainer = findViewById(R.id.wormholes);
      for (int i = 0; i < wormholeItemContainer.getChildCount(); i++) {
        View itemView = wormholeItemContainer.getChildAt(i);
        Star star = (Star) itemView.getTag();
        if (star.getWormholeExtra().getEmpireID() == empire.getID()) {
          refreshWormhole(itemView);
        }
      }*/
    }
  };
}
