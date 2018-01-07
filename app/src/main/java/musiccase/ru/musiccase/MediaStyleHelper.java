package musiccase.ru.musiccase;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

class MediaStyleHelper {
    public static NotificationCompat.Builder from(
            Context context, MediaSessionCompat mediaSession) {
        MediaControllerCompat controller = mediaSession.getController();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
       try{
           builder
                   .setColor(Color.BLACK)
                   .setOngoing(false)
                   .setAutoCancel(true)
                   .setContentTitle(Metadata.title)
                   .setContentText(Metadata.artist)
                   .setSubText("")
                   .setLargeIcon(Metadata.bitmap)
                   .setContentIntent(controller.getSessionActivity())
                   .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
           TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
           stackBuilder.addNextIntent(new Intent(context, HomePage.class));

           PendingIntent resultPendingIntent =
                   stackBuilder.getPendingIntent(
                           0,
                           PendingIntent.FLAG_NO_CREATE
                   );
           builder.setContentIntent(resultPendingIntent);
       }catch (Exception e){
           Log.e(Const.LOG_TAG, e.getMessage());
       }

        return builder;
    }
}
