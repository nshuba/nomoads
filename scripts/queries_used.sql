-- Row 1 --
-- SELECT * FROM per_app_labels where predicted_package_name = 1
-- and predicted_general = 0 AND ats = 0 AND
-- (list_labels_easy_list = 1 or list_labels_easy_privacy = 1 or list_labels_moaab = 1)

-- Row 2 --
SELECT * FROM per_app_labels where predicted_package_name = 1
and predicted_general = 0 AND ats = 0 AND
(list_labels_easy_list = 1 or list_labels_easy_privacy = 1 or list_labels_moaab = 1)

-- Row 3 --
-- SELECT * FROM per_app_labels where predicted_package_name = 1
-- and predicted_general = 0 AND ats = 0 AND
-- list_labels_easy_list = 0 and list_labels_easy_privacy = 0 and list_labels_moaab = 0

-- -- Row 4 --
-- SELECT * FROM per_app_labels where predicted_package_name = 1
-- and predicted_general = 0 AND ats = 0 AND
-- list_labels_easy_list = 0 and list_labels_easy_privacy = 0 and list_labels_moaab = 0

-- -- Row 5 --
-- SELECT * FROM per_app_labels where predicted_package_name = 0
-- and predicted_general = 0 AND ats = 1 AND
-- (list_labels_easy_list = 1 or list_labels_easy_privacy = 1 or list_labels_moaab = 1)

-- -- Row 6 --
-- SELECT * FROM per_app_labels where predicted_package_name = 0
-- and predicted_general = 1 AND ats = 1 AND
-- (list_labels_easy_list = 1 or list_labels_easy_privacy = 1 or list_labels_moaab = 1)

--UPDATE hook_count
--SET developer = (SELECT developer FROM app_devs_certs
--							WHERE package_name = hook_count.package_name)

--SELECT count(DISTINCT developer), type FROM hook_count GROUP BY type

