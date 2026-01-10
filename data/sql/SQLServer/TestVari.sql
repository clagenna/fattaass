USE aass
GO

/****** Object:  View dbo.GASLettureMensili    Script Date: 06/01/2026 12:14:05 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE OR ALTER VIEW dbo.H2OLettureMensili
AS
SELECT te.NomeIntesta
	  ,ft.annoComp
	  ,ft.periodEffDtIniz
	  ,ft.periodEffDtFine
      -- ,le.idLettura
      ,le.idH2OFattura as idFattura
      ,le.lettQtaMc
      ,le.LettData
      ,le.TipoLett
      -- ,le.matricola
      -- ,le.coeffK
      ,le.Consumofatt
  FROM H2OLettura AS le
	  INNER JOIN H2OFattura as ft
		 ON ft.idH2OFattura=le.idH2OFattura
	  INNER JOIN intesta as te
		 ON ft.idIntesta=te.idIntesta











FROM GASLettura as le
	  INNER JOIN GASFattura as ft
		 ON ft.idGASFattura=le.idGASFattura
	  INNER JOIN intesta as te
		 ON ft.idIntesta=te.idIntesta
WHERE  1=1
GO


