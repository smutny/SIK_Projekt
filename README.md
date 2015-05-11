# SIK_Projekt
## Założenia projektu
* protokół FTP
* obsługuje tylko logowanie anonymous bez hasła
* nawiązuje połączenie w trybie passive lub active
* pozwala na przeglądanie jednego katalogu

Założenia projektowe
Opis implementowanego zakresu protokołu
Lista funkcjonalności aplikacji

## Opis implementowanego zakresu protokołu
Projekt wykorzystuje odpowiednie porty protokołu FTP w trybie pasywnym/aktywnym. Wykorzystuje komendy związane z protokołem FTP do komunikacji na linii Serwer - Klient.

## Lista funkcjonalności aplikacji
Aplikacja umożliwia uruchomienie serwera, połączenie się za pomocą komendy "java Client [nazwaHosta]", wylistowanie zawartości folderu z podziałem na podfoldery i pliki, logowanie anonymous, przerwanie połączenia, połączenie z wieloma klientami na raz.