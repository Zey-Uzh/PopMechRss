package ru.zeyuzh.testrssreader;


public enum RSSsections {



    all(R.string.section_all,"http://www.popmech.ru/out/public-all.xml"),
    science(R.string.section_science,"http://www.popmech.ru/out/public-science.xml"),
    weapon(R.string.section_weapon,"http://www.popmech.ru/out/public-weapon.xml"),
    technologies(R.string.section_technologies,"http://www.popmech.ru/out/public-technologies.xml"),
    vehicles(R.string.section_vehicles,"http://www.popmech.ru/out/public-vehicles.xml"),
    gadgets(R.string.section_gadgets,"http://www.popmech.ru/out/public-gadgets.xml"),
    lectures_popular(R.string.section_lectures_popular,"http://www.popmech.ru/out/public-lectures-popular.xml"),
    commercial(R.string.section_commercial,"http://www.popmech.ru/out/public-commercial.xml"),
    business_news(R.string.section_business_news,"http://www.popmech.ru/out/public-business-news.xml"),
    editorial(R.string.section_editorial,"http://www.popmech.ru/out/public-editorial.xml"),
    history(R.string.section_history,"http://www.popmech.ru/out/public-history.xml"),
    made_in_russia(R.string.section_made_in_russia,"http://www.popmech.ru/out/public-made-in-russia.xml"),
    adrenalin(R.string.section_adrenalin,"http://www.popmech.ru/out/public-adrenalin.xml"),
    diy(R.string.section_diy,"http://www.popmech.ru/out/public-diy.xml"),
    design(R.string.section_design,"http://www.popmech.ru/out/public-design.xml");

    private int value;
    private String url;

    RSSsections (int value, String url)
    {
        this.value = value;
        this.url = url ;
    }

    public int getValue() {
        return value;
    }

    public String getRSSsectionUrl() {
            return url;
        }

    @Override
    public String toString() {
        return url;
    }
}
