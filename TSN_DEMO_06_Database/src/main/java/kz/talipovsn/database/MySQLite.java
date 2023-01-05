package kz.talipovsn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 24; // НОМЕР ВЕРСИИ БАЗЫ ДАННЫХ И ТАБЛИЦ !

    static final String DATABASE_NAME = "books"; // Имя базы данных

    static final String TABLE_NAME = "book_types"; // Имя таблицы
    static final String ID = "id"; // Поле с ID
    static final String NAME = "name"; // Поле с наименованием организации
    static final String NAME_LC = "name_lc"; // // Поле с наименованием организации в нижнем регистре
    static final String BOOK_GENRE = "book_genre"; // Жанр книги
    static final String PUBLICATION_DATE = "pub_date"; // Дата издания
    static final String PRICE = "price"; //Цена
    static final String AGE_LIMIT = "age_limit"; //Возрастное ограничение


    static final String ASSETS_FILE_NAME = "books.txt"; // Имя файла из ресурсов с данными для БД
    static final String DATA_SEPARATOR = "|"; // Разделитель данных в файле ресурсов с телефонами

    private Context context; // Контекст приложения

    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Метод создания базы данных и таблиц в ней
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + NAME + " TEXT,"
                + NAME_LC + " TEXT,"
                + BOOK_GENRE + " TEXT"
                + PUBLICATION_DATE + "TEXT"
                + PRICE + "TEXT"
                + AGE_LIMIT + "TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        System.out.println(CREATE_CONTACTS_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME,  db);
    }

    // Метод при обновлении структуры базы данных и/или таблиц в ней
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        System.out.println("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление нового контакта в БД
    public void addData(SQLiteDatabase db, String name, String bookGenre, String pubDate, String price, String ageLimit) {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(NAME_LC, name.toLowerCase());
        values.put(BOOK_GENRE, bookGenre);
        values.put(PUBLICATION_DATE, pubDate);
        values.put(PRICE, price);
        values.put(AGE_LIMIT, ageLimit);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    String name = st.nextToken().trim(); // Извлекаем из строки название организации без пробелов на концах
                    String bookGenre = st.nextToken().trim();
                    String pubDate = st.nextToken().trim();
                    String price = st.nextToken().trim();
                    String ageLimit = st.nextToken().trim();
                    addData(db, name, bookGenre, pubDate, price, ageLimit);
                }
            }

        // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter) {

        String selectQuery; // Переменная для SQL-запроса

        if (filter.equals("")) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + NAME;
        } else {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + NAME_LC + " LIKE '%" +
                    filter.toLowerCase() + "%'" +
                    " OR " + BOOK_GENRE + " LIKE '%" + filter + "%'" +
                    " OR " + PUBLICATION_DATE + " LIKE '%" + filter + "%'" +
                    " OR " + PRICE + " LIKE '%" + filter + "%'" +
                    " OR " + AGE_LIMIT + " LIKE '%" + filter + "%'" + ") ORDER BY " + NAME;
        }
        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД
        Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

        StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса

        int num = 0;
        if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
            do { // Цикл по всем записям результата запроса
                int v = cursor.getColumnIndex(NAME);
                int f = cursor.getColumnIndex(BOOK_GENRE);
                int u = cursor.getColumnIndex(PUBLICATION_DATE);
                int j = cursor.getColumnIndex(PRICE);
                int z = cursor.getColumnIndex(AGE_LIMIT);
                String name = cursor.getString(v);
                String booGenre = cursor.getString(f);
                String pubDate = cursor.getString(u);
                String price = cursor.getString(j);
                String ageLimit = cursor.getString(z);
                data.append(String.valueOf(++num) + ") Название: " + name +
                        "\n Жанр: " + booGenre +
                        "\n Дата издания: " + pubDate +
                        "\n Цена: " + price +
                        "\n Возрастное ограничение: " + ageLimit + "\n");
            } while (cursor.moveToNext()); // Цикл пока есть следующая запись
        }
        return data.toString(); // Возвращение результата
    }

}