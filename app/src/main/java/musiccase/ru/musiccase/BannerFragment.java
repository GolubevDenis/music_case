package musiccase.ru.musiccase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * Этот класс отвечает за баннеры на домешней страницы
 */
public class BannerFragment extends Fragment {

    final int TIME_NEXT_PAGE = 7000;
    private ViewPager viewPager;
    private Context context;
    private SimplePagerAdapter adapter;
    private File fileInfo;
    private boolean isFileInfoLoad = false;
    private ArrayList<Banner> banners = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        context = getContext();
        new LoaderBannerInfo(banners).execute(Const.URL_DATA_BANNERS);
        while (!isFileInfoLoad){} //ожидание окончания загрузки info.xml
        parseInfo();
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_banner, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new SimplePagerAdapter();
        viewPager.setAdapter(adapter);
        timer.schedule(new UpdateTimeTask(adapter.getCount()), 1, TIME_NEXT_PAGE);
    }

    private void parseInfo(){
        NodeList nodeList;
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(fileInfo);

            document.getDocumentElement().normalize();
            nodeList = document.getElementsByTagName("banner");
            for (int i = 0; i < nodeList.getLength(); i++) {
                banners.add(getBanner(nodeList.item(i)));
                if(adapter!=null) adapter.notifyDataSetChanged();
            }
        }catch (Exception e){
            Log.d(Const.LOG_TAG, "[BannerFragment.parseInfo()]  ошибка");
        }
    }

    private Banner getBanner(Node node) {
        Banner banner = new Banner();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            banner.setNameFile(getTagValue("name", element));
            banner.setUrl(getTagValue("url", element));
        }

        return banner;
    }
    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }


    //Кастомный адаптер
    private class SimplePagerAdapter extends PagerAdapter {

        private final LayoutInflater inflater;

        SimplePagerAdapter() {
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return banners.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final Banner banner = banners.get(position);
            View view = inflater.inflate(R.layout.banner_item, container, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_banner);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(banner.getUrl()));
                    startActivity(i);
                }
            });
            banner.setImageView(imageView);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    //загрузчик информации о баннерах

    private class LoaderBannerInfo extends AsyncTask<String, Void, Void> {

        ArrayList<Banner> banners;

        private LoaderBannerInfo(ArrayList<Banner> banners) {
            this.banners = banners;
        }

        protected Void doInBackground(String... urls) {
            try {
                fileInfo = new File(context.getFilesDir(), "info.xml");
                try {
                    InputStream in = new java.net.URL(Const.URL_DATA_BANNERS).openStream();
                    FileOutputStream out = new FileOutputStream(fileInfo);
                    int a;
                    do{
                        a = in.read();
                        if(a == -1)break;
                        out.write(a);
                    }while (true);
                    in.close();
                    out.close();

                } catch (Exception e) {
                    Log.d(Const.LOG_TAG, e.getMessage());
                }
            } catch (Exception e) {
                Log.d(Const.LOG_TAG, e.getMessage());
            }
            isFileInfoLoad = true;
            return null;
        }


        protected void onPostExecute(Bitmap result) {
           Log.d(Const.LOG_TAG, "[BannerFragment.LoaderBannerInfo.onPostExecute()]  Загрузка info.xml завершена");
        }
    }


    Timer timer = new Timer();

    private class UpdateTimeTask extends TimerTask {
        int mLength = 0;

        UpdateTimeTask(final int length) {
            this.mLength = length;
        }

        public void run() {
            viewPager.post(new Runnable() {
                @Override
                public void run() {
                    if (viewPager.getCurrentItem()+1 < mLength) {
                        Updateviewpager(viewPager.getCurrentItem() + 1);
                    } else {
                        Updateviewpager(0);
                    }
                }
            });
        }
    }

    public void Updateviewpager(int position) {
        viewPager.setCurrentItem(position, true);
    }
}
