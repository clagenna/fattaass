I'm struggling with log output from library org.apache pdfbox while converting a PDF to List&lt;String&gt;.
Here's a simple test class to show what appens:

	 @Test
	  public void doTheJob() throws InvalidPasswordException, IOException {
	    Path pth = Paths.get("data/EE_2020-08-01_2020-12-31.pdf");
	    try (PDDocument pdf = PDDocument.load(pth.toFile()); StringWriter swr = new StringWriter();) {
	      new PDFDomTree().writeText(pdf, swr);
	      outHtml = new ArrayList<>();
	      String[] arr = swr.toString().split("\n");
	      outHtml.addAll(Arrays.asList(arr));
	    }
	    outHtml //
	        .stream() //
	        .forEach(System.out::println);
	  }
 
 If I run this test I get a lot of INFO logs on my console from   <code>org.mabb.fontverter.opentype.TtfInstructions.TtfInstructionParser</code> class.