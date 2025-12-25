USE aass
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.EESumLettAttuale') AND type in (N'FN', N'IF', N'TF', N'FS', N'FT'))
BEGIN
execute dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Claudio
-- Create date: 16/10/2023
-- Description:	sum delle letture attuale per idFattura
-- =============================================
CREATE FUNCTION dbo.EESumLettAttuale ( @p1 int )
RETURNS int
AS
BEGIN
	DECLARE @Result int

	SELECT @result = sum(le.lettAttuale) 
	  FROM dbo.EELettura as le
     WHERE idEEFattura = @p1

	RETURN @Result

END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.toAnnoMese') AND type in (N'FN', N'IF', N'TF', N'FS', N'FT'))
BEGIN
execute dbo.sp_executesql @statement = N'-- =============================================
-- Author:		Claudio
-- Create date: 16/10/23
-- Description:	converte Date in un decimal(6,2) per anno,mese
-- =============================================
CREATE FUNCTION dbo.toAnnoMese ( @p1 date )
RETURNS decimal(6,2)
AS
BEGIN
	DECLARE @Result decimal(6,2) =	CONVERT(decimal(6,2), cast(year(@p1) as float) + cast(datepart(M,@p1) as float) / 100) 
	RETURN @Result
END
' 
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.EEFattura') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.EEFattura(
	idEEFattura int IDENTITY(1,1) NOT NULL,
	idIntesta int NULL,
	annoComp int NULL,
	DataEmiss date NULL,
	fattNrAnno int NULL,
	fattNrNumero nvarchar(50) NULL,
	periodFattDtIniz date NULL,
	periodFattDtFine date NULL,
	CredPrecKwh int NULL,
	CredAttKwh int NULL,
	addizFER money NULL,
	impostaQuiet money NULL,
	TotPagare money NULL,
	nomeFile varchar(128) NULL,
 CONSTRAINT PK_EEFatture PRIMARY KEY CLUSTERED 
(
	idEEFattura ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.EEConsumo') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.EEConsumo(
	idEEConsumo int IDENTITY(1,1) NOT NULL,
	idEEFattura int NOT NULL,
	tipoSpesa nvarchar(2) NULL,
	dtIniz date NULL,
	dtFine date NULL,
	stimato int NULL,
	prezzoUnit decimal(10, 6) NULL,
	quantita decimal(8, 2) NULL,
	importo money NULL,
 CONSTRAINT PK_EEConsumi PRIMARY KEY CLUSTERED 
(
	idEEConsumo ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.EELettura') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.EELettura(
	idLettura int IDENTITY(1,1) NOT NULL,
	idEEFattura int NOT NULL,
	LettDtPrec date NULL,
	LettPrec int NULL,
	TipoLettura varchar(16) NULL,
	LettDtAttuale date NULL,
	LettAttuale int NULL,
	LettConsumo float NULL,
 CONSTRAINT PK_EELetture PRIMARY KEY CLUSTERED 
(
	idLettura ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.Intesta') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.Intesta(
	idIntesta int NOT NULL,
	NomeIntesta nvarchar(64) NULL,
	dirfatture nvarchar(128) NULL,
 CONSTRAINT PK_Intesta PRIMARY KEY CLUSTERED 
(
	idIntesta ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.EEConsumoMensile'))
EXEC dbo.sp_executesql @statement = N'

CREATE VIEW dbo.EEConsumoMensile
AS
SELECT te.NomeIntesta,           
       cs.idEEFattura,           
       cs.dtIniz,           
	   YEAR(cs.dtIniz) as annoComp,
       -- dbo.toAnnoMese(cs.dtIniz) as meseComp,
	   format(year(cs.dtiniz), ''0000'') + ''-'' + format(month(cs.dtIniz), ''00'') as meseComp,
       cs.tipoSpesa,           
       cs.prezzoUnit,           
       cs.quantita,           
       cs.importo      
   FROM EEConsumo AS cs           
     INNER JOIN EEFattura AS ft 
         ON ft.idEEFattura = cs.idEEFattura           
     INNER JOIN intesta AS te 
         ON ft.idIntesta = te.idIntesta     
 WHERE 1=1
   AND 0 < ( 
				SELECT SUM(LettAttuale) 
				  FROM EELettura 
				 WHERE idEEFattura=ft.idEEFattura
		)
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.EECostoAnnuo'))
EXEC dbo.sp_executesql @statement = N'CREATE VIEW dbo.EECostoAnnuo
AS
SELECT NomeIntesta
      ,annoComp
      ,SUM(importo) as totAnno
  FROM aass.dbo.EEConsumoMensile
GROUP BY NomeIntesta, annoComp
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.GASConsumo') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.GASConsumo(
	idConsumo int IDENTITY(1,1) NOT NULL,
	idGASFattura int NOT NULL,
	tipoSpesa varchar(4) NULL,
	dtIniz date NULL,
	dtFine date NULL,
	stimato int NULL,
	prezzoUnit decimal(10, 6) NULL,
	quantita decimal(8, 2) NULL,
	importo money NULL,
 CONSTRAINT PK_GASConsumi PRIMARY KEY CLUSTERED 
(
	idConsumo ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.GASLettura') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.GASLettura(
	idLettura int IDENTITY(1,1) NOT NULL,
	idGASFattura int NOT NULL,
	lettQtaMc int NULL,
	LettData date NULL,
	TipoLett varchar(16) NULL,
	Consumofatt float NULL,
 CONSTRAINT PK_GASLetture PRIMARY KEY CLUSTERED 
(
	idLettura ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.GASFattura') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.GASFattura(
	idGASFattura int IDENTITY(1,1) NOT NULL,
	idIntesta int NULL,
	annoComp int NULL,
	DataEmiss date NULL,
	fattNrAnno int NULL,
	fattNrNumero nvarchar(50) NULL,
	periodFattDtIniz date NULL,
	periodFattDtFine date NULL,
	periodEffDtIniz date NULL,
	periodEffDtFine date NULL,
	periodAccontoDtIniz date NULL,
	periodAccontoDtFine date NULL,
	accontoBollPrec money NULL,
	addizFER money NULL,
	impostaQuiet money NULL,
	TotPagare money NULL,
	nomeFile varchar(128) NULL,
 CONSTRAINT PK_GASFatture PRIMARY KEY CLUSTERED 
(
	idGASFattura ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.GASConsumoMensile'))
EXEC dbo.sp_executesql @statement = N'
CREATE VIEW dbo.GASConsumoMensile
AS
SELECT te.NomeIntesta 
      ,cs.idGASFattura
      ,YEAR(cs.dtIniz) as annoComp
      ,dbo.toAnnoMese(cs.dtIniz) as meseComp
      ,cs.dtIniz as dtIniz
      ,cs.dtFine as dtFine
      ,cs.tipoSpesa
      ,cs.prezzoUnit
      ,cs.quantita
	  ,DATEDIFF(d,cs.dtIniz, cs.dtFine) + 1 as qtaGG
      ,cs.quantita / (DATEDIFF(d,cs.dtIniz, cs.dtFine) + 1 ) as mediaGG
      ,cs.importo
  FROM GASConsumo as cs
	  INNER JOIN GASFattura as ft
		 ON ft.idGASFattura=cs.idGASFattura
	  INNER JOIN intesta as te
		 ON ft.idIntesta=te.idIntesta
WHERE  1=1
   --  AND ft.periodEffDtIniz IS NOT NULL
	  AND cs.dtIniz BETWEEN
	      -- ft.periodEffDtIniz  
		  ( SELECT MIN(lt.lettData) 
		      FROM GASLettura as lt
		     WHERE lt.idGASFattura=ft.idGASFattura )
	   AND 
	      -- ft.periodEffDtFine
  		  ( SELECT MAX(lt.lettData) 
		      FROM GASLettura as lt
		     WHERE lt.idGASFattura=ft.idGASFattura )

' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.GASCostoAnnuo'))
EXEC dbo.sp_executesql @statement = N'
CREATE VIEW dbo.GASCostoAnnuo
AS
SELECT NomeIntesta
      ,YEAR(dtIniz) as annoComp
      ,SUM(importo) as costoAnnuo
  FROM aass.dbo.GASConsumoMensile
GROUP BY NomeIntesta, YEAR(dtIniz) 
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.GASLettureMensili'))
EXEC dbo.sp_executesql @statement = N'CREATE VIEW dbo.GASLettureMensili
AS
SELECT te.NomeIntesta
      ,ft.idGASFattura
      ,ft.annoComp
      ,ft.periodAccontoDtIniz
      ,ft.periodAccontoDtFine
      ,ft.periodEffDtIniz
      ,ft.periodEffDtFine
      ,ft.periodFattDtIniz
      ,ft.periodFattDtFine
      ,le.LettData
      ,le.lettQtaMc
      ,le.TipoLett
      ,le.Consumofatt  
FROM GASLettura as le
	  INNER JOIN GASFattura as ft
		 ON ft.idGASFattura=le.idGASFattura
	  INNER JOIN intesta as te
		 ON ft.idIntesta=te.idIntesta
WHERE  1=1
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.GASLettureMensiliTipo'))
EXEC dbo.sp_executesql @statement = N'CREATE VIEW dbo.GASLettureMensiliTipo
AS
SELECT NomeIntesta
--      ,annoComp
        , COALESCE(periodAccontoDtIniz,periodEffDtIniz,periodFattDtIniz) as dtIniz
        , COALESCE(periodAccontoDtFine,periodEffDtFine,periodFattDtFine) as dtFine
--      ,periodAccontoDtFine
--      ,periodEffDtIniz
--      ,periodEffDtFine
--      ,periodFattDtIniz
--      ,periodFattDtFine
--      ,LettData
      ,SUM(ISNULL((case when tipoLett = ''eff''  then lettQtaMc end), 0)) as qtaMcEff
      ,SUM(ISNULL((case when tipoLett = ''stim'' then lettQtaMc end), 0)) as qtaMcStim
      ,SUM(ISNULL((case when tipoLett = ''auto'' then lettQtaMc end), 0)) as qtaMcAuto
      ,SUM(ISNULL((case when tipoLett = ''tot''  then lettQtaMc end), 0)) as qtaMcTot

      ,SUM(ISNULL((case when tipoLett = ''eff''  then Consumofatt end), 0)) as ConsumofattEff
      ,SUM(ISNULL((case when tipoLett = ''stim'' then Consumofatt end), 0)) as ConsumofattStim
      ,SUM(ISNULL((case when tipoLett = ''auto'' then Consumofatt end), 0)) as ConsumofattAuto
      ,SUM(ISNULL((case when tipoLett = ''tot''  then Consumofatt end), 0)) as ConsumofattTot

FROM GASLettureMensili
GROUP BY NomeIntesta
        , COALESCE(periodAccontoDtIniz,periodEffDtIniz,periodFattDtIniz)
        , COALESCE(periodAccontoDtFine,periodEffDtFine,periodFattDtFine)
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.H2OFattura') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.H2OFattura(
	idH2OFattura int IDENTITY(1,1) NOT NULL,
	idIntesta int NULL,
	annoComp int NULL,
	DataEmiss date NULL,
	fattNrAnno int NULL,
	fattNrNumero nvarchar(50) NULL,
	periodFattDtIniz date NULL,
	periodFattDtFine date NULL,
	periodCongDtIniz date NULL,
	periodCongDtFine date NULL,
	periodAccontoDtIniz date NULL,
	periodAccontoDtFine date NULL,
	assicurazione money NULL,
	impostaQuiet money NULL,
	RestituzAccPrec money NULL,
	TotPagare money NULL,
	nomeFile varchar(128) NULL,
 CONSTRAINT PK_H2OFatture PRIMARY KEY CLUSTERED 
(
	idH2OFattura ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.viewFatture'))
EXEC dbo.sp_executesql @statement = N'
CREATE VIEW dbo.viewFatture AS
SELECT ''EE'' as tipofatt
     , eef.idEEFattura as idFattura
     , eef.idIntesta
	 , inn.nomeIntesta
	 , eef.annoComp
	 , eef.DataEmiss
	 , eef.periodFattDtIniz as dtIniz
	 , eef.periodFattDtFine as dtFine
	 , eef.TotPagare as totFattura
	 , inn.dirfatture + ''\'' + eef.nomeFile as fullPath
   FROM EEFattura as eef
     INNER JOIN dbo.Intesta as inn
	    ON eef.idIntesta = inn.idIntesta
UNION
SELECT ''GAS'' as tipofatt
     , eef.idGASFattura as idFattura
     , eef.idIntesta
	 , inn.nomeIntesta
	 , eef.annoComp
	 , eef.DataEmiss
	 , eef.periodFattDtIniz as dtIniz
	 , eef.periodFattDtFine as dtFine
	 , eef.TotPagare as totFattura
	 , inn.dirfatture + ''\'' + eef.nomeFile as fullPath
   FROM GASFattura as eef
     INNER JOIN dbo.Intesta as inn
	    ON eef.idIntesta = inn.idIntesta
UNION
SELECT ''H2O'' as tipofatt
     , eef.idH2OFattura as idFattura
     , eef.idIntesta
	 , inn.nomeIntesta
	 , eef.annoComp
	 , eef.DataEmiss
	 , eef.periodFattDtIniz as dtIniz
	 , eef.periodFattDtFine as dtFine
	 , eef.TotPagare as totFattura
	 , inn.dirfatture + ''\'' + eef.nomeFile as fullPath
   FROM H2OFattura as eef
     INNER JOIN dbo.Intesta as inn
	    ON eef.idIntesta = inn.idIntesta
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.EELettureMensili'))
EXEC dbo.sp_executesql @statement = N'


CREATE VIEW dbo.EELettureMensili
AS
SELECT te.NomeIntesta
      ,le.idLettura
      ,le.idEEFattura
	  ,YEAR(le.LettDtAttuale) as annoComp
	  ,FORMAT(YEAR(le.LettDtAttuale), ''0000'') + ''-'' + FORMAT(MONTH(le.LettDtAttuale), ''00'') meseComp
	  ,le.LettDtPrec 
      -- ,dbo.toAnnoMese(le.LettDtPrec) as meseDtPrec
	  ,le.TipoLettura
      ,le.LettPrec
	  ,le.LettDtAttuale
      -- ,dbo.toAnnoMese(le.LettDtAttuale) as meseDtAttuale
      ,le.LettAttuale
      ,le.LettConsumo
	  , DATEDIFF(d, le.LettDtPrec, le.LettDtAttuale) as qtaGG
  FROM dbo.EELettura as le
  INNER JOIN dbo.EEFattura as ft
		 ON ft.idEEFattura=le.idEEFattura
	  INNER JOIN dbo.intesta as te
		 ON ft.idIntesta=te.idIntesta
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.EEConsumoAnnuo'))
EXEC dbo.sp_executesql @statement = N'


CREATE VIEW dbo.EEConsumoAnnuo
AS
SELECT NomeIntesta
      ,annoComp
      ,SUM(quantita) as totAnno
  FROM aass.dbo.EEConsumoMensile
GROUP BY NomeIntesta, annoComp
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.EEScaglioniImporti'))
EXEC dbo.sp_executesql @statement = N'
CREATE view dbo.EEScaglioniImporti
AS
SELECT 
	NomeIntesta,
	annoComp,
	meseComp,
	dtIniz,
	ISNULL(E1, 0) AS Sca1,
	ISNULL(E2, 0) AS Sca2,
	ISNULL(E3, 0) AS Sca3,
	ISNULL(S1, 0) AS SpreadSc1,
	ISNULL(S2, 0) AS SpreadSc2,
	ISNULL(PU, 0) AS Pun,
	ISNULL(R,  0) AS Rifiuti,
	ISNULL(P,  0) AS ImpegnoPot,
	ISNULL(E1, 0) +
	ISNULL(E2, 0) +
	ISNULL(E3, 0) +
	ISNULL(S1, 0) +
	ISNULL(S2, 0) +
	ISNULL(PU, 0) +
	ISNULL(R,  0) +
	ISNULL(P,  0)  AS TotRiga
 FROM (
	SELECT NomeIntesta
		 , annoComp
		 , meseComp
	     , dtIniz
		 , tipoSpesa
		 , importo 
	  FROM EEConsumoMensile
 ) consunit  PIVOT (
	SUM(importo)
	FOR tipoSpesa in ( E1,E2, E3, S1, S2, PU,  R, P )
 ) AS pivot_cons
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.EEScaglioniPrezzoUnit'))
EXEC dbo.sp_executesql @statement = N'
CREATE VIEW dbo.EEScaglioniPrezzoUnit
AS
SELECT 
	NomeIntesta,
	annoComp,
	meseComp,
	ISNULL(E1, 0) AS EESca1,
	ISNULL(E2, 0) AS EESca2,
	ISNULL(E3, 0) AS EESca3,
	ISNULL(S1, 0) AS EESpreadSca1,
	ISNULL(S2, 0) AS EESpreadSca2,
	ISNULL(PU, 0) AS EEPun,
	ISNULL(R,  0) AS Rifiuti,
	ISNULL(P,  0) AS ImpegnoPot
	--ISNULL(E1, 0) +
	--ISNULL(E2, 0) +
	--ISNULL(E3, 0) +
	--ISNULL(S1, 0) +
	--ISNULL(S2, 0) +
	--ISNULL(PU, 0) +
	--ISNULL(R,  0) +
	--ISNULL(P,  0)  AS TotRiga
 FROM (
	SELECT NomeIntesta,
	       annoComp,
		   meseComp,
	       tipoSpesa,
		   ISNULL(prezzoUnit,0) as prezzoUnit
	  FROM EEConsumoMensile
 ) consunit  PIVOT (
	SUM(prezzoUnit)
	FOR tipoSpesa in ( E1,E2, E3, S1, S2, PU,  R, P )
 ) AS pivot_cons
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.EESpeseMensili'))
EXEC dbo.sp_executesql @statement = N'
CREATE VIEW dbo.EESpeseMensili
as
SELECT te.nomeIntesta,
	   dbo.toAnnoMese(cs.dtIniz) as dtIniz,
       SUM(cs.quantita) as quantita,
	   SUM(cs.importo) as importo
  FROM dbo.EEConsumo as cs
   	    INNER JOIN dbo.EEFattura as ft
        ON ft.idEEFattura=cs.idEEFattura
        INNER JOIN dbo.intesta as te
        ON ft.idIntesta=te.idIntesta
  WHERE dbo.EESumLettAttuale(cs.idEEFattura) > 0
GROUP BY te.nomeIntesta, dbo.toAnnoMese(cs.dtIniz)
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.GASScaglioniImporto'))
EXEC dbo.sp_executesql @statement = N'CREATE VIEW dbo.GASScaglioniImporto
AS
SELECT NomeIntesta
     , idGASFattura
     , dtIniz	
     , dtFine
	 , qtaGG
	 , ISNULL(G1,0) AS Scagl1
	 , ISNULL(G2,0) AS Scagl2
	 , ISNULL(G3,0) AS Scagl3
	 , ISNULL(G1,0) +
	   ISNULL(G2,0) +
	   ISNULL(G3,0) AS Totale
	 --, (ISNULL(G1,0) +
	 --  ISNULL(G2,0) +
	 --  ISNULL(G3,0) ) / qtaGG as mediaGG
  FROM (
	SELECT NomeIntesta
	     , idGASFattura
		 , tipoSpesa
		 , dtIniz
		 , dtFine
		 , (DATEDIFF(d, dtIniz, dtFine) + 1) as qtaGG
		 , ISNULL(importo, 0) as importo
     FROM 	dbo.GASConsumoMensile
) consunit PIVOT (
  SUM(importo)
  FOR tipoSpesa in ( G1, G2, G3 )
) AS pivCons

' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.GASScaglioniPrezzoUnit'))
EXEC dbo.sp_executesql @statement = N'

CREATE VIEW dbo.GASScaglioniPrezzoUnit
AS
SELECT NomeIntesta
     , idGASFattura
     , dtIniz	
     , dtFine
	 , qtaGG
	 , ISNULL(G1,0) AS Scagl1
	 , ISNULL(G2,0) AS Scagl2
	 , ISNULL(G3,0) AS Scagl3
	 , ISNULL(G1,0) +
	   ISNULL(G2,0) +
	   ISNULL(G3,0) AS Totale
	 , (ISNULL(G1,0) +
	    ISNULL(G2,0) +
	    ISNULL(G3,0) ) / qtaGG as mediaGG
  FROM (
	SELECT NomeIntesta
	     , idGASFattura
		 , tipoSpesa
		 , dtIniz
		 , dtFine
		 , (DATEDIFF(d, dtIniz, dtFine) + 1) as qtaGG
		 , ISNULL(quantita, 0) as quantita
     FROM 	dbo.GASConsumoMensile
) consunit PIVOT (
  SUM(quantita)
  FOR tipoSpesa in ( G1, G2, G3 )
) AS pivCons

' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.GASConsumoAnnuo'))
EXEC dbo.sp_executesql @statement = N'

CREATE VIEW dbo.GASConsumoAnnuo
AS
SELECT NomeIntesta
      ,YEAR(dtIniz) as annoComp
      ,SUM(quantita) as consumoAnnuo
  FROM aass.dbo.GASConsumoMensile
GROUP BY NomeIntesta, YEAR(dtIniz) 
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.H2OConsumo') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.H2OConsumo(
	idConsumo int IDENTITY(1,1) NOT NULL,
	idH2OFattura int NOT NULL,
	tipoSpesa varchar(4) NULL,
	dtIniz date NULL,
	dtFine date NULL,
	stimato int NULL,
	prezzoUnit decimal(10, 6) NULL,
	quantita decimal(8, 2) NULL,
	importo money NULL,
 CONSTRAINT PK_H2OConsumi PRIMARY KEY CLUSTERED 
(
	idConsumo ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.H2OConsumoMensile'))
EXEC dbo.sp_executesql @statement = N'

CREATE VIEW dbo.H2OConsumoMensile
AS
SELECT te.NomeIntesta 
      ,cs.idH2OFattura
      ,YEAR(cs.dtIniz) as annoComp
      ,cs.dtIniz
      ,cs.dtFine
	  -- ,dbo.EESumLettAttuale(cs.idEEFattura) as totLett
      ,cs.tipoSpesa
      ,cs.prezzoUnit
      ,cs.quantita
      ,cs.importo
  FROM aass.dbo.H2OConsumo as cs
	  INNER JOIN dbo.H2OFattura as ft
		 ON ft.idH2OFattura=cs.idH2OFattura
	  INNER JOIN dbo.intesta as te
		 ON ft.idIntesta=te.idIntesta
--   WHERE dbo.EESumLettAttuale(cs.idH2OFattura) > 0
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.H2OConsumoAnnuo'))
EXEC dbo.sp_executesql @statement = N'
CREATE VIEW dbo.H2OConsumoAnnuo
AS
SELECT NomeIntesta
      ,YEAR(dtIniz) as annoComp
      ,SUM(importo) as totAnno
  FROM aass.dbo.H2OConsumoMensile
GROUP BY NomeIntesta, YEAR(dtIniz) 
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'dbo.H2OScaglioniImporto'))
EXEC dbo.sp_executesql @statement = N'CREATE VIEW dbo.H2OScaglioniImporto
AS
SELECT NomeIntesta,
       annoComp,
       dtIniz,
       dtFine,
       ISNULL(SUM(case when tipospesa = ''S1'' then importo end), 0) as Sca1,
       ISNULL(SUM(case when tipospesa = ''S2'' then importo end), 0) as Sca2,
       ISNULL(SUM(case when tipospesa = ''S3'' then importo end), 0) as Sca3,
       ISNULL(SUM(case when tipospesa = ''S4'' then importo end), 0) as Sca4,

       ISNULL(SUM(case when tipospesa = ''TA1'' then importo end), 0) as TarA1,
       ISNULL(SUM(case when tipospesa = ''TA2'' then importo end), 0) as TarA2,
       ISNULL(SUM(case when tipospesa = ''TA3'' then importo end), 0) as TarA3,
       ISNULL(SUM(case when tipospesa = ''TA4'' then importo end), 0) as TarA4,

       ISNULL(SUM(case when tipospesa = ''F'' then importo end), 0) as quotaFissa
   FROM H2OConsumoMensile
GROUP BY NomeIntesta, annoComp, dtIniz, dtFine
' 
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.H2OLettura') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.H2OLettura(
	idLettura int IDENTITY(1,1) NOT NULL,
	idH2OFattura int NOT NULL,
	lettQtaMc int NULL,
	LettData date NULL,
	TipoLett varchar(16) NULL,
	Consumofatt float NULL,
 CONSTRAINT PK_H2OLetture PRIMARY KEY CLUSTERED 
(
	idLettura ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.mesegg') AND type in (N'U'))
BEGIN
CREATE TABLE dbo.mesegg(
	mese int NOT NULL,
	qtagg int NOT NULL
) ON PRIMARY
END
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


INSERT dbo.Intesta (idIntesta, NomeIntesta, dirfatture) VALUES (1, N'claudio', N'F:\Google Drive\SMichele\AASS')
GO
INSERT dbo.Intesta (idIntesta, NomeIntesta, dirfatture) VALUES (2, N'andrea', N'F:\varie\AASS\Andrea')
GO
INSERT dbo.Intesta (idIntesta, NomeIntesta, dirfatture) VALUES (3, N'alessandro', N'F:\varie\AASS\Alessandro')
GO
INSERT dbo.Intesta (idIntesta, NomeIntesta, dirfatture) VALUES (4, N'pippo', N'f:\varie\AASS\pippo')
GO


INSERT dbo.mesegg (mese, qtagg) VALUES (1, 31)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (2, 28)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (3, 31)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (4, 30)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (5, 31)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (6, 30)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (7, 31)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (8, 31)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (9, 30)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (10, 31)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (11, 30)
GO
INSERT dbo.mesegg (mese, qtagg) VALUES (12, 31)
GO


IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.EEFattura') AND name = N'UX_EEFattura_dtEmiss')
CREATE UNIQUE NONCLUSTERED INDEX UX_EEFattura_dtEmiss ON dbo.EEFattura
(
	idIntesta ASC,
	DataEmiss ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
GO
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.GASFattura') AND name = N'UX_GASFattura_dtEmiss')
CREATE NONCLUSTERED INDEX UX_GASFattura_dtEmiss ON dbo.GASFattura
(
	idIntesta ASC,
	DataEmiss ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
GO
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID(N'dbo.H2OFattura') AND name = N'UX_H2OFattura_dtEmiss')
CREATE NONCLUSTERED INDEX UX_H2OFattura_dtEmiss ON dbo.H2OFattura
(
	idIntesta ASC,
	DataEmiss ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_EEConsumo_EEFattura') AND parent_object_id = OBJECT_ID(N'dbo.EEConsumo'))
ALTER TABLE dbo.EEConsumo  WITH CHECK ADD  CONSTRAINT FK_EEConsumo_EEFattura FOREIGN KEY(idEEFattura)
REFERENCES dbo.EEFattura (idEEFattura)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_EEConsumo_EEFattura') AND parent_object_id = OBJECT_ID(N'dbo.EEConsumo'))
ALTER TABLE dbo.EEConsumo CHECK CONSTRAINT FK_EEConsumo_EEFattura
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_EEFattura_Intesta') AND parent_object_id = OBJECT_ID(N'dbo.EEFattura'))
ALTER TABLE dbo.EEFattura  WITH CHECK ADD  CONSTRAINT FK_EEFattura_Intesta FOREIGN KEY(idIntesta)
REFERENCES dbo.Intesta (idIntesta)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_EEFattura_Intesta') AND parent_object_id = OBJECT_ID(N'dbo.EEFattura'))
ALTER TABLE dbo.EEFattura CHECK CONSTRAINT FK_EEFattura_Intesta
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_EELettura_EEFattura') AND parent_object_id = OBJECT_ID(N'dbo.EELettura'))
ALTER TABLE dbo.EELettura  WITH CHECK ADD  CONSTRAINT FK_EELettura_EEFattura FOREIGN KEY(idEEFattura)
REFERENCES dbo.EEFattura (idEEFattura)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_EELettura_EEFattura') AND parent_object_id = OBJECT_ID(N'dbo.EELettura'))
ALTER TABLE dbo.EELettura CHECK CONSTRAINT FK_EELettura_EEFattura
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_GASConsumo_GASFattura') AND parent_object_id = OBJECT_ID(N'dbo.GASConsumo'))
ALTER TABLE dbo.GASConsumo  WITH CHECK ADD  CONSTRAINT FK_GASConsumo_GASFattura FOREIGN KEY(idGASFattura)
REFERENCES dbo.GASFattura (idGASFattura)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_GASConsumo_GASFattura') AND parent_object_id = OBJECT_ID(N'dbo.GASConsumo'))
ALTER TABLE dbo.GASConsumo CHECK CONSTRAINT FK_GASConsumo_GASFattura
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_GASFattura_Intesta') AND parent_object_id = OBJECT_ID(N'dbo.GASFattura'))
ALTER TABLE dbo.GASFattura  WITH CHECK ADD  CONSTRAINT FK_GASFattura_Intesta FOREIGN KEY(idIntesta)
REFERENCES dbo.Intesta (idIntesta)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_GASFattura_Intesta') AND parent_object_id = OBJECT_ID(N'dbo.GASFattura'))
ALTER TABLE dbo.GASFattura CHECK CONSTRAINT FK_GASFattura_Intesta
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_GASLettura_GASFattura') AND parent_object_id = OBJECT_ID(N'dbo.GASLettura'))
ALTER TABLE dbo.GASLettura  WITH CHECK ADD  CONSTRAINT FK_GASLettura_GASFattura FOREIGN KEY(idGASFattura)
REFERENCES dbo.GASFattura (idGASFattura)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_GASLettura_GASFattura') AND parent_object_id = OBJECT_ID(N'dbo.GASLettura'))
ALTER TABLE dbo.GASLettura CHECK CONSTRAINT FK_GASLettura_GASFattura
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_H2OConsumo_H2OFattura') AND parent_object_id = OBJECT_ID(N'dbo.H2OConsumo'))
ALTER TABLE dbo.H2OConsumo  WITH CHECK ADD  CONSTRAINT FK_H2OConsumo_H2OFattura FOREIGN KEY(idH2OFattura)
REFERENCES dbo.H2OFattura (idH2OFattura)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_H2OConsumo_H2OFattura') AND parent_object_id = OBJECT_ID(N'dbo.H2OConsumo'))
ALTER TABLE dbo.H2OConsumo CHECK CONSTRAINT FK_H2OConsumo_H2OFattura
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_H2OFattura_Intesta') AND parent_object_id = OBJECT_ID(N'dbo.H2OFattura'))
ALTER TABLE dbo.H2OFattura  WITH CHECK ADD  CONSTRAINT FK_H2OFattura_Intesta FOREIGN KEY(idIntesta)
REFERENCES dbo.Intesta (idIntesta)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_H2OFattura_Intesta') AND parent_object_id = OBJECT_ID(N'dbo.H2OFattura'))
ALTER TABLE dbo.H2OFattura CHECK CONSTRAINT FK_H2OFattura_Intesta
GO
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_H2OLettura_H2OFattura') AND parent_object_id = OBJECT_ID(N'dbo.H2OLettura'))
ALTER TABLE dbo.H2OLettura  WITH CHECK ADD  CONSTRAINT FK_H2OLettura_H2OFattura FOREIGN KEY(idH2OFattura)
REFERENCES dbo.H2OFattura (idH2OFattura)
GO
IF  EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'dbo.FK_H2OLettura_H2OFattura') AND parent_object_id = OBJECT_ID(N'dbo.H2OLettura'))
ALTER TABLE dbo.H2OLettura CHECK CONSTRAINT FK_H2OLettura_H2OFattura
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.cercaBuchiDateConsumoEE') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE dbo.cercaBuchiDateConsumoEE AS' 
END
GO
 ALTER  PROCEDURE dbo.cercaBuchiDateConsumoEE(  @Nomeintesta varchar(128)  )
 AS
DECLARE	@dtMin decimal(6,2),
		@dtMax decimal(6,2),
		@dec decimal(6,2),
		@dtCurr date
		


SELECT @dtMin = min(dtIniz), 
	   @dtMax = max(dtIniz)
  FROM dbo.EEConsumoMensile
  WHERE NomeIntesta=@Nomeintesta


SET @dtCurr = CAST( REPLACE( CAST(@dtMin as  varchar(18)) + '.01', '.', '-') as date)
SET @dec = dbo.toAnnoMese(@dtCurr)

CREATE TABLE #miedate ( miadt decimal(6,2) )

WHILE @dec <= @dtMax
BEGIN
   INSERT into #miedate VALUES (@dec) 
   SET @dtCurr = DATEADD(m, 1, @dtCurr)
   SET @dec = dbo.toAnnoMese(@dtCurr)
END

SELECT *
   FROM #miedate
   WHERE miadt NOT IN 
		( SELECT   dtIniz
            FROM aass.dbo.EEConsumoMensile
           WHERE NomeIntesta=@Nomeintesta
	    )
DROP TABLE #miedate
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'dbo.deleteFatture') AND type in (N'P', N'PC'))
BEGIN
EXEC dbo.sp_executesql @statement = N'CREATE PROCEDURE dbo.deleteFatture AS' 
END
GO
ALTER Procedure dbo.deleteFatture( @idItesta int = 1 ) 
AS

DELETE FROM EEconsumo 
	WHERE idEEFattura in ( 
		SELECT idEEFattura 
		  FROM dbo.EEFattura as ft
		    INNER JOIN dbo.Intesta as te
			  ON ft.idIntesta=te.idIntesta
		    WHERE te.idIntesta=@idItesta
		)



DELETE FROM EELettura 
	WHERE idEEFattura in ( 
		SELECT idEEFattura 
		  FROM dbo.EEFattura as ft
		    INNER JOIN dbo.Intesta as te
			  ON ft.idIntesta=te.idIntesta
		    WHERE te.idIntesta=@idItesta
		)

DELETE FROM EEFattura
	where idIntesta = @idItesta



DELETE FROM GASconsumo 
	WHERE idGASFattura in ( 
		SELECT idGASFattura 
		  FROM dbo.GASFattura as ft
		    INNER JOIN dbo.Intesta as te
			  ON ft.idIntesta=te.idIntesta
		    WHERE te.idIntesta=@idItesta
		)



DELETE FROM GASLettura 
	WHERE idGASFattura in ( 
		SELECT idGASFattura 
		  FROM dbo.GASFattura as ft
		    INNER JOIN dbo.Intesta as te
			  ON ft.idIntesta=te.idIntesta
		    WHERE te.idIntesta=@idItesta
		)

DELETE FROM GASFattura
	where idIntesta = @idItesta


DELETE FROM H2Oconsumo 
	WHERE idH2OFattura in ( 
		SELECT idH2OFattura 
		  FROM dbo.H2OFattura as ft
		    INNER JOIN dbo.Intesta as te
			  ON ft.idIntesta=te.idIntesta
		    WHERE te.idIntesta=@idItesta
		)



DELETE FROM H2OLettura 
	WHERE idH2OFattura in ( 
		SELECT idH2OFattura 
		  FROM dbo.H2OFattura as ft
		    INNER JOIN dbo.Intesta as te
			  ON ft.idIntesta=te.idIntesta
		    WHERE te.idIntesta=@idItesta
		)

DELETE FROM H2OFattura
	where idIntesta = @idItesta

GO
