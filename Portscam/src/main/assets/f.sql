USE [mouzy20sep]
GO
/****** Object:  StoredProcedure [Printer].[getPendingKot]    Script Date: 10/13/2021 18:06:17 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER procedure [Printer].[getPendingKot] as begin
begin try
with itemnetwork(name, catagory, network) as (select i.ItemName, c.name, c.networkprinter from Item_Registration as i inner join Catagory as c on c.name = i.catagory group by c.networkprinter, c.Name, i.ItemName)
    update kts    set kts.name=bi.name ,kts.mobno=bi.mobno,kts.address=bi.address,kts.parcelno=bi.tockenno from Kot_Information kts inner join bill_information bi on bi.billno=kts.billno where kts.billno in      ( SELECT b.billno
        FROM Kot_Information B
                 INNER JOIN Kot_Particulars KP ON KP.kotno = B.KotNo
                 inner join itemnetwork as i on i.name = kp.ItemName
        where b.KotNo in (select distinct billno from printer.printer where type = 'K'));
           with itemnetwork(name, catagory, network) as (
            select i.ItemName, c.name, c.networkprinter
            from Item_Registration as i
                     inner join Catagory as c on c.name = i.catagory
            group by c.networkprinter, c.Name, i.ItemName)
with itemnetwork(name, catagory, network) as (select i.ItemName, c.name, c.networkprinter from Item_Registration as i inner join Catagory as c on c.name = i.catagory group by c.networkprinter, c.Name, i.ItemName)
SELECT b.*, kp.*, i.catagory catagory, i.network as printer FROM Kot_Information b INNER JOIN Kot_Particulars KP ON KP.kotno = B.KotNo inner join itemnetwork as i on i.name = kp.ItemName inner join Bill_information as bb on bb.billno = b.BillNo where b.autoprint = 0 and b.aps = 'Y'
end try
begin catch
end catch;
end