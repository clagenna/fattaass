SELECT TOP (1000) idGASFattura, nomeFile
  FROM [aass].[dbo].[GASFattura]
  where 1=1
   and idGASFattura in (407, 408)


SELECT * FROM GASLettura
   WHERE 1=1
    and idGASFattura in ( 407, 408 )
   


SELECT * FROM GASConsumo
   WHERE idGASFattura in ( 391 )


SELECT * FROM GASLettureMensili
   WHERE idGASFattura in ( 407, 408 )


SELECT * FROM GASFattura fa
WHERE 0 = ( SELECT COUNT(*) FROM GASConsumo co WHERE  co.idGASFattura=fa.idGASFattura )











create or alter view checkGASFattureOrfane
as
SELECT * FROM GASFattura fa
WHERE fa.idIntesta = 1
  AND (    0 = ( SELECT COUNT(*) FROM GASLettura le WHERE  le.idGASFattura=fa.idGASFattura )
       OR  0 = ( SELECT COUNT(*) FROM GASConsumo co WHERE  co.idGASFattura=fa.idGASFattura )
	  )
GO


SELECT * FROM H2OFattura
WHERE 1=1
  
ORDER BY DataEmiss



SELECT * FROM H2OLettura
   WHERE 1=1
    -- and idGASFattura in ( 407, 408 )
ORDER BY LettData
GO

DECLARE @idFatt int = 1198
DELETE FROM H2OConsumo WHERE idH2OFattura = @idFatt
DELETE FROM H2OLettura WHERE idH2OFattura = @idFatt
DELETE FROM H2OFattura WHERE idH2OFattura = @idFatt


USE aass
GO

DECLARE @RC int
DECLARE @idItesta int = 1

EXECUTE @RC = deleteAllFatture @idItesta
GO



