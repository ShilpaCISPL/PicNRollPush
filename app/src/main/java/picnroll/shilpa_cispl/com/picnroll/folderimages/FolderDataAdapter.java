package picnroll.shilpa_cispl.com.picnroll.folderimages;

/**
 * Created by shilpa-cispl on 23/10/17.
 */

public class FolderDataAdapter {
    public String ImageURL;
    public String ImageTitle;

    public String getImageUrl() {

        return ImageURL;
    }

    public void setImageUrl(String imageServerUrl) {

        this.ImageURL = imageServerUrl;
    }

    public String getImageTitle() {

        return ImageTitle;
    }

    public void setImageTitle(String Imagetitlename) {

        this.ImageTitle = Imagetitlename;
    }
}
