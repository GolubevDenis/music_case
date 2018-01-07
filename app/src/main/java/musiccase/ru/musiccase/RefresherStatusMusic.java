package musiccase.ru.musiccase;


import java.util.ArrayList;
import java.util.List;

class RefresherStatusMusic {
    static final List<Refreshable> list = new ArrayList<>();
    static void registerRefresh(Refreshable r){
        list.add(r);
    }
    static void unregisterRefresh(Refreshable r){
        list.remove(r);
    }
    static void fullRefresh(){
        for(Refreshable r : list){
            try{
                r.refresh();
            }catch (Exception e){}
        }
    }
    static final List<Refreshable> listMetadata = new ArrayList<>();
    static void registerUpdateMetadata(Refreshable r){
        listMetadata.add(r);
    }
    static void unregisterUpdateMetadata(Refreshable r){
        listMetadata.remove(r);
    }
    static void fullUpdateMetadata(){
        for(Refreshable r : listMetadata){
            try{
                r.updateMetadata();
            }catch (Exception e){}
        }
    }
}
