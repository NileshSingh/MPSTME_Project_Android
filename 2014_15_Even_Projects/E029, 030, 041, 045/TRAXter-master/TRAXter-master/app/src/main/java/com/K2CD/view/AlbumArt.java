package com.TRAXter.view;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import com.TRAXterv2.R;
import com.TRAXter.model.Track;
import com.TRAXter.persistance.turtle.db.TurtleDatabase;
import com.TRAXter.preferences.Preferences;
import com.TRAXter.presentation.AlbumArtResolver;
import com.TRAXter.presentation.InstanceFormatter;

/**
 * TURTLE PLAYER
 * <p/>
 * Licensed under MIT & GPL
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * More Information @ www.turtle-player.co.uk
 *
 * @author Simon Honegger (Hoene84)
 */

public class AlbumArt
{
	public enum Type
	{
		RIGHT(-1, com.TRAXterv2.R.id.albumArtRight), // FIXME: should be 1, if your know why its inverted, change it
		LEFT(1, com.TRAXterv2.R.id.albumArtLeft), // FIXME: should be -1, your know why its inverted, change it
		CENTER(0, com.TRAXterv2.R.id.albumArt);

		private final double horizontalShift;
		private final int rId;

		Type(double horizontalShift, int rId)
		{
			this.horizontalShift = horizontalShift;
			this.rId = rId;
		}

		public double getHorizontalShift()
		{
			return horizontalShift;
		}

		public int getRId()
		{
			return rId;
		}
	}

	private final View albumArtView;

	private final Type type;

	private final ImageView albumArt;
	private final TextView title;
	private final TextView artist;
	private final TextView album;

	private final TurtleDatabase db;
	private  AsyncTask<Track, Void, Bitmap> actualAsyncTask = null;

	public AlbumArt(View albumArtViewGroup,
						 Type type,
						 TurtleDatabase db)
	{
		this.db = db;

		albumArtView = albumArtViewGroup.findViewById(type.getRId());
		this.type = type;

		albumArt = (ImageView) albumArtView.findViewById(com.TRAXterv2.R.id.picture);
		title = (TextView) albumArtView.findViewById(com.TRAXterv2.R.id.trackTitle);
		artist = (TextView) albumArtView.findViewById(com.TRAXterv2.R.id.trackArtist);
		album = (TextView) albumArtView.findViewById(com.TRAXterv2.R.id.trackAlbum);

		albumArtView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
		{
			public boolean onPreDraw()
			{
				setInitialPositions();
				albumArtView.getViewTreeObserver().removeOnPreDrawListener(this);
				return true;
			}
		});

	}

	public View getAlbumArtView()
	{
		return albumArtView;
	}

	public void setTrack(final Track track)
	{
		setTrackDigest(track);

		if(track != null){
			actualAsyncTask = new AlbumArtResolver(db){

				@Override
				protected Bitmap doInBackground(Track... params)
				{
					if(actualAsyncTask == this)
					{
						return super.doInBackground(params);
					}
					return null;
				}

				@Override
				protected void onPostExecute(Bitmap bitmap)
				{
					if(actualAsyncTask == this && bitmap != null)
					{
						albumArt.setImageBitmap(bitmap);
						Log.v(Preferences.TAG, "albumart for " + track.getFullPath() + " resolved");
					}
				}
			}.execute(track);
		}
	}

	/**
	 * update View synchronous with attributes easy to resolve
	 * @param track
	 */
	public void setTrackDigest(final Track track)
	{
		if(track != null){
			title.setText(track.accept(InstanceFormatter.SHORT_WITH_NUMBER));
			artist.setText(track.GetArtist().accept(InstanceFormatter.SHORT));
			album.setText(track.GetAlbum().accept(InstanceFormatter.SHORT));
		}
		else
		{
			title.setText("");
			artist.setText("");
			album.setText("");
		}
		albumArt.setImageDrawable(albumArtView.getResources().getDrawable(com.TRAXterv2.R.drawable.blank_album_art));
		albumArtView.invalidate();
	}

	public void setInitialPositions()
	{
		albumArtView.setVisibility(View.VISIBLE);
		albumArtView.scrollTo((int)(type.getHorizontalShift() * albumArtView.getWidth()), 0);
	}
}
