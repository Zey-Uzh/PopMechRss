package ru.zeyuzh.testrssreader;


public enum RSSsections {

    all("http://www.popmech.ru/out/public-all.xml", R.string.section_all),
    science("http://www.popmech.ru/out/public-science.xml", R.string.section_science),
    weapon("http://www.popmech.ru/out/public-weapon.xml", R.string.section_weapon),
    technologies("http://www.popmech.ru/out/public-technologies.xml", R.string.section_technologies),
    vehicles("http://www.popmech.ru/out/public-vehicles.xml", R.string.section_vehicles),
    gadgets("http://www.popmech.ru/out/public-gadgets.xml", R.string.section_gadgets),
    lectures_popular("http://www.popmech.ru/out/public-lectures-popular.xml", R.string.section_lectures_popular),
    commercial("http://www.popmech.ru/out/public-commercial.xml", R.string.section_commercial),
    business_news("http://www.popmech.ru/out/public-business-news.xml", R.string.section_business_news),
    editorial("http://www.popmech.ru/out/public-editorial.xml", R.string.section_editorial),
    history("http://www.popmech.ru/out/public-history.xml", R.string.section_history),
    made_in_russia("http://www.popmech.ru/out/public-made-in-russia.xml", R.string.section_made_in_russia),
    adrenalin("http://www.popmech.ru/out/public-adrenalin.xml", R.string.section_adrenalin),
    diy("http://www.popmech.ru/out/public-diy.xml", R.string.section_diy),
    design("http://www.popmech.ru/out/public-design.xml", R.string.section_design);

    private String url;
    private int id;

    RSSsections (String url, int id)
    {
        this.url = url ;
        this.id = id;
    }

    public String getRSSsectionUrl() {
            return url;
        }

    public int getRSSsectionId() {
        return id;
    }

    @Override
    public String toString() {
        return url;
    }
}
