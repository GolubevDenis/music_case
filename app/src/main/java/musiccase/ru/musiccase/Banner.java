package musiccase.ru.musiccase;

import android.widget.ImageView;

class Banner {
    private String nameFile;
    private String url;
    Banner(String nameFile, String url) {
        this.nameFile = nameFile;
        this.url = url;
    }
    Banner(){}
    String getNameFile() {return nameFile;}
    void setNameFile(String nameFile) {this.nameFile = nameFile;}
    String getUrl() {return url;}
    String getUrlUmage(){return Const.URL_BANNERS + nameFile;}
    void setUrl(String url) {this.url = url;}
    void setImageView(ImageView imageView) {
        new DownloadImageTask(imageView).execute(getUrlUmage());
    }
}
