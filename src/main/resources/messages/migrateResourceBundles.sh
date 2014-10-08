# Run this script after updating the resources bundles

native2ascii -encoding utf8 messages.properties messages.properties
native2ascii -encoding utf8 messages_de.properties messages_de.properties
native2ascii -encoding utf8 messages_es.properties messages_es.properties
native2ascii -encoding utf8 messages_fr.properties messages_fr.properties
native2ascii -encoding utf8 messages_he.properties messages_he.properties
native2ascii -encoding utf8 messages_hu.properties messages_hu.properties
native2ascii -encoding utf8 messages_it.properties messages_it.properties
native2ascii -encoding utf8 messages_sv.properties messages_sv.properties

# These two should be renamed and may need more regions
native2ascii -encoding utf8 messages_zh_hans.properties messages_zh.properties
native2ascii -encoding utf8 messages_zh_hant.properties messages_zh_TW.properties