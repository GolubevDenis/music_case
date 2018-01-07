package musiccase.ru.musiccase;

//этот интерфейт реализуют те классы обьектам которых нужно обновлять граффический интерфейс
//в зависимости от статуса аудио потока
public interface Refreshable {
    void refresh();
    void updateMetadata();
}
