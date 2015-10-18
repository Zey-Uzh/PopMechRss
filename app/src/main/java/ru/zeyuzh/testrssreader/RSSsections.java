package ru.zeyuzh.testrssreader;


public enum RSSsections {



    all("http://www.popmech.ru/out/public-all.xml"),
    science("http://www.popmech.ru/out/public-science.xml"),
    weapon("http://www.popmech.ru/out/public-weapon.xml"),
    technologies("http://www.popmech.ru/out/public-technologies.xml"),
    vehicles("http://www.popmech.ru/out/public-vehicles.xml"),
    gadgets("http://www.popmech.ru/out/public-gadgets.xml"),
    lectures_popular("http://www.popmech.ru/out/public-lectures-popular.xml"),
    commercial("http://www.popmech.ru/out/public-commercial.xml"),
    business_news("http://www.popmech.ru/out/public-business-news.xml"),
    editorial("http://www.popmech.ru/out/public-editorial.xml"),
    history("http://www.popmech.ru/out/public-history.xml"),
    made_in_russia("http://www.popmech.ru/out/public-made-in-russia.xml"),
    adrenalin("http://www.popmech.ru/out/public-adrenalin.xml"),
    diy("http://www.popmech.ru/out/public-diy.xml"),
    design("http://www.popmech.ru/out/public-design.xml");

    private String url;

    RSSsections (String url)
    {
        this.url = url ;
    }
    public String getRSSsectionUrl() {
            return url;
        }

    @Override
    public String toString() {
        return url;
    }
}
