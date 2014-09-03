SELECT ID, IMAGE_URL FROM SHOUTOUTS WHERE IMAGE_URL NOT IN (
  SELECT DISTINCT
    IMAGE_URL
  FROM SHOUTOUTS
  WHERE IS_VIEWED = 0 and IS_CLEANED = 0
) AND IMAGE_URL IN (
  SELECT DISTINCT
    IMAGE_URL
  FROM SHOUTOUTS
  WHERE IS_VIEWED = 1 and IS_CLEANED = 0
) AND IS_CLEANED = 0