-- query 1 myself, more like substring 
SELECT * from player 
WHERE lower(name) LIKE '%' || lower(login) || '%'             
ORDER BY login asc;


-- query 2, golden,  
SELECT DISTINCT x.day, x.realm, x.theme
FROM quest x, loot r
WHERE 
	x.theme = r.theme AND x.realm = r.realm AND x.day = r.day AND r.treasure LIKE '%Gold%' AND r.login IS NOT NULL
ORDER BY day, realm, theme;


-- query 3, evening 
SELECT x.theme, x.day, x.realm, x.succeeded
FROM quest x
WHERE
	succeeded > cast('20:00:00' as time) OR succeeded IS NULL
ORDER BY  theme, day, realm;


-- query 4, cheat
WITH 
     helper1 (login, name, day, quest) AS (
          SELECT pl.login, pl.name, ac.day, COUNT(*) AS qt
     	  FROM  actor ac, player Pl
	  WHERE  pl.login = ac.login
	  GROUP BY pl.login, pl.name, ac.day
	  HAVING COUNT(*) > 1
	  ORDER BY login, name, day
     )

SELECT h1.login, h1.name, ac.day, ac.realm, ac.theme
FROM actor ac, helper1 h1 
WHERE h1.login = ac.login AND h1.day = ac.day;


-- query 5 , bend  
with helper (login, total) as (
    SELECT pl.login, count(av.name)
    FROM player pl, avatar av
    WHERE 
      pl.login = av.login 
    GROUP BY pl.login
  )

SELECT Pl.login, pl.name, pl.gender, hp.total AS avatar
FROM player pl, avatar av, helper hp
WHERE 
	pl.login = av.login AND pl.login = hp.login AND pl.gender <> av.gender
GROUP BY pl.login, pl.gender, pl.name, hp.total
ORDER BY pl.login;


-- query 6, successful
(SELECT DISTINCT theme FROM quest) 
except 
(SELECT DISTINCT theme FROM quest WHERE succeeded IS NULL)
ORDER BY theme;


-- query 7, frequency
with 
visithelper(login, realm, visits) AS 
(
    SELECT login, realm, COUNT(*)
    FROM visit 
    GROUP BY login, realm
    HAVING COUNT(*) > 1 
    ORDER BY login, realm
), 
dayfirst(login, realm, DAY) AS (
    SELECT login, realm, MIN(DAY)
    FROM visit 
    GROUP BY login, realm
    ORDER BY login, realm
), 
daylast(login, realm, DAY) AS (
    SELECT login, realm, MAX(DAY)
    FROM visit  
    GROUP BY login, realm
    ORDER BY login, realm
), 
daytotal(login, realm, days) AS (
    SELECT df.login, df.realm, CAST((dl.day::DATE - df.day::DATE) AS DOUBLE PRECISION)
    FROM dayfirst df, daylast dl    
    WHERE df.login = dl.login AND df.realm = dl.realm
    ORDER BY login, realm
)

SELECT vh.login, vh.realm, vh.visits, 
CAST((dt.days/(vh.visits-1)) AS DECIMAL(5,2)) AS frequency
FROM  visithelper vh,  daytotal dt
WHERE vh.login = dt.login AND vh.realm = dt.realm
ORDER BY vh.login, vh.realm;


-- query 8, race   
with 
		helper1 (realm, gender, race, worth) AS (
			SELECT act.realm,avt.gender, avt.race, sum(trs.sql)
			FROM avatar avt, actor act, quest qst, visit vst, loot lt, treasure trs
			WHERE
				lt.treasure = trs.treasure
				AND 
                lt.theme = qst.theme
				AND 
                lt.realm = qst.realm
				AND 
                lt.day = qst.day
				AND 
                lt.login = act.login
				AND 
                lt.realm = act.realm
				AND 
                lt.day =   act.day
				AND 
                lt.theme = act.theme
				AND 
                act.theme = qst.theme
				AND 
                act.realm = qst.realm
				AND 
                act.day = qst.day
				AND 
                act.login = vst.login
				AND 
                act.realm = vst.realm
				AND 
                act.day = vst.day
				AND 
                qst.succeeded IS NOT NULL
				AND 
                vst.login = avt.login
				AND 
                vst.name = avt.name
				AND 
                act.login = avt.login
			GROUP BY act.realm, avt.gender, avt.race
		),
		
		helper2 (realm, gender, race, total) AS (
			SELECT h1.realm, h1.gender, h1.race, max(h1.worth)
			FROM avatar avt, helper1 h1
			WHERE
				avt.gender = h1.gender AND avt.race = h1.race
			GROUP BY h1.realm, h1.race, h1.gender
		)
		
SELECT h2.realm, h2.race, h2.gender, h2.total
FROM helper1 h1, helper2 h2
WHERE 
	h1.realm = h2.realm AND h1.gender = h2.gender AND h1.race = h2.race AND h1.worth = h2.total
ORDER BY h2.realm, h2.race, h2.gender;



-- query 9, companions 
with helper1 as (
    SELECT DISTINCT vs.login, vs.name, ac.day, ac.realm, ac.theme
    FROM actor ac, visit vs
    WHERE 
    ac.login = vs.login 
    AND 
    ac.realm = vs.realm 
    AND 
    ac.day = vs.day
), 
helper2 as (
    SELECT DISTINCT h1.login as companion1, h1.name as fname, 
    h1.realm as realm, h2.login as companion2, h2.name as lname
    FROM helper1 h1, helper1 h2
    WHERE 
    h1.login < h2.login 
    AND 
    h1.name <> h2.name
    AND
    h1.realm = h1.realm
    AND 
    h1.theme = h2.theme
    AND
    h1.day = h2.day
)

SELECT * FROM helper2 
WHERE NOT EXISTS (
    (
        SELECT helper1.day, helper1.realm, helper1.theme
        FROM helper1
        WHERE 
        helper1.login = helper2.companion1 
        AND 
        helper1.name = helper2.fname
        AND
        helper1.realm = helper2.realm
    )
    EXCEPT
    (
        SELECT helper1.day, helper1.realm, helper1.theme
        FROM helper1
        WHERE 
        helper1.login = helper2.companion2
        AND
        helper1.name = helper2.lname
        AND  
        helper1.realm = helper2.realm
    )
) 
and NOT EXISTS (
    (
        SELECT helper1.day, helper1.realm, helper1.theme
        FROM helper1
        WHERE 
        helper1.login = helper2.companion2
        AND
        helper1.name = helper2.lname
        AND
        helper1.realm = helper2.realm
    )
    EXCEPT 
    (
        SELECT helper1.day, helper1.realm, helper1.theme
        FROM helper1
        WHERE 
        helper1.login = helper2.companion1
        AND
        helper1.name = helper2.fname
        AND
        helper1.realm = helper2.realm
    )
) 
ORDER BY  realm, companion1, fname, companion2, lname;



-- query 10, potential
with
helper1(login , name , race) AS
(
SELECT av.login , av.name , av.race
FROM actor acr, avatar av 
WHERE acr.login = av.login
ORDER BY av.login , av.name , av.race
),

helperVisit(login , name , realm , day , theme) AS
(
SELECT vs.login , vs.name , vs.realm , vs.day ,acr.theme
FROM visit vs, actor acr
WHERE vs.login = acr.login AND vs.realm = acr.realm AND vs.day = acr.day
ORDER BY vs.login , vs.name , vs.realm , vs.day , acr.theme
),

helperQuest(login , name , total) AS 
(
SELECT hv.login , hv.name , count(*) AS total 
FROM helperVisit hv, quest qst 
WHERE hv.theme = qst.theme AND hv.realm = qst.realm AND hv.day = qst.day AND qst.succeeded IS NOT NULL
GROUP BY hv.login , hv.name
),

helpDecider(theme , realm , day , sql) AS
(
SELECT lt.theme , lt.realm , lt.day , trs.sql 
FROM  loot lt, treasure trs, quest qst
WHERE lt.treasure = trs.treasure AND qst.theme = lt.theme AND qst.realm = lt.realm AND qst.day = lt. day AND qst.succeeded IS NOT NULL
ORDER BY lt.theme , lt.realm , lt.day , trs.sql 
),

helperMax(theme , realm , day , max) AS
(
SELECT hd.theme , hd.realm , hd.day , max(hd.sql)
FROM helpDecider hd
GROUP BY hd.theme , hd.realm , hd.day
),

playerHelper(login , name , maxsql) AS
(
SELECT hv.login , hv.name , sum(hm.max)
FROM helperMax hm , helperVisit hv
WHERE hv.realm = hm.realm AND hv.theme = hm.theme AND hv.day = hm.day
GROUP BY hv.login , hv.name 
),

helperX(login , name) AS
(
SELECT login , name  
FROM avatar avt
except
SELECT ph.login , ph.name
FROM playerHelper ph
),

mergeHelper(login , name , race , sql , total) AS
(
SELECT ph.login , ph.name , h1.race , ph.maxsql , hq.total
FROM playerHelper ph , helperQuest hq , helper1 h1
WHERE ph.login = hq.login AND ph.name = hq.name AND h1.login = ph.login AND h1.name = ph.name AND h1.login = hq.login AND h1.name = hq.name
UNION
SELECT hx.login , hx.name , h1.race , 0 , 0
FROM helperX hx , helper1 h1
WHERE h1.login = hx.login AND h1.name = hx.name
)

SELECT mh.login AS login , mh.name AS name , mh.race AS race , mh.sql AS earned , mh.total AS quest
FROM mergeHelper mh
ORDER BY mh.login , mh.name , mh.race , mh.sql , mh.total;
