package com.TRAXter.persistance.turtle.mapping;

import android.database.Cursor;
import com.TRAXter.model.SongDigest;
import com.TRAXter.persistance.framework.creator.ResultCreator;
import com.TRAXter.persistance.turtle.db.structure.Tables;

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

public class SongCreator implements ResultCreator<Tables.SongsReadable, SongDigest, Cursor>
{

    public SongDigest create(Cursor source)
    {
        return new SongDigest(source.getString(source.getColumnIndex(Tables.SongsReadable.TITLE.getName())));
    }
}
