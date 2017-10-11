# A media player based on vlc.

## Theme

    amber,
    black,
    blue,
    cyan,
    deep_orange
    deep_purple,
    green,
    indigo,
    light_blue,
    light_green,
    lime,
    orange,
    pink,
    purple,
    red,
    teal,

    amber,black,blue,cyan,deep_orangedeep_purple,green,indigo,light_blue,light_green,lime,orange,pink,purple,red,teal,

    black,red,pink,purple,deep_purple,indigo,blue,light_blue,cyan,teal,green,light_green,lime,amber,orange,deep_orange


THEMES="amber black blue cyan deep_orange deep_purple green indigo light_blue light_green lime orange pink purple red teal"

SOURCE_FILE=gridview_progressbar.xml

for THEME in $THEMES; do
    cp $SOURCE_FILE gridview_progressbar_$THEME.xml
done

THEMES="amber black blue cyan deep_orange deep_purple green indigo light_blue light_green lime orange pink purple red teal"

for THEME in $THEMES; do
    sed -i '' "s/\?attr\/colorAccent/\@color\/theme_color_${THEME}_accent/g" gridview_progressbar_${THEME}.xml
done

sed -i '' "s/\?attr\/colorAccent/\@color\/theme_color_amber_accent/g" gridview_progressbar_amber.xml

