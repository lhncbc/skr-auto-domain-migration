package gov.nih.nlm.semmed.tags;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;

/**
 * 
 * A tag library for creating the index to a (html) table
 * 
 * @author rodriguezal
 *
 */
public class TablePager extends TagSupport {

	private static final long serialVersionUID = 1L;

	private int currentPage;

	private int count;

	private String link;
	
	private int max = 20;

	public void setCurrentPage(int s) {
		currentPage = s;
	}

	public void setCount(int s) {
		count = s;
	}

	public void setLink(String s) {
		link = s;
	}
	
	public void setMax(int s){
		max = s;
	}

	public int doStartTag() {
		try {
			JspWriter out = pageContext.getOut();

			if (count > 0) {
				out.print("Showing " + (currentPage * max + 1) + " to "
						+ (Math.min(count, (currentPage + 1) * max)));
				if (count > max) {
					out.print("<br>");

					int lastPage = (int) Math.ceil(count / (double)max);

					out.print("Pages: ");

					if (currentPage == 0)
						out.print("Prev ");
					else
						out.print("<a href=\""
								+ link.replaceAll("\\{\\}", ""
										+ (currentPage - 1)) + "\"> Prev </a>");

					if (lastPage > 6) {
						if (currentPage == 0)
							out.print("1 | ");
						else
							out.print("<a href=\""
									+ link.replaceAll("\\{\\}", "0")
									+ "\"> 1 </a>| ");

						if (currentPage > 2)
							out.print("...|");

						for (int i = Math.max(1, currentPage - 2); (i <= currentPage + 2)
								&& i < lastPage; i++) {
							if (i != currentPage)
								out.print("<a href=\""
										+ link.replaceAll("\\{\\}", "" + i)
										+ "\"> " + (i + 1) + "</a> | ");
							else
								out.print(" " + (i + 1) + " | ");

						}

						if (currentPage + 2 < lastPage) {
							out.print("...|");
							out.print("<a href=\""
									+ link.replaceAll("\\{\\}", ""
											+ (lastPage - 1)) + "\"> "
									+ (lastPage) + "</a> | ");
						}
					} else {
						for (int i = 0; i < lastPage; i++) {
							if (i != currentPage)
								out.print("<a href=\""
										+ link.replaceAll("\\{\\}", ""
												+ i ) + "\"> " + (i + 1)
										+ "</a> | ");
							else
								out.print(" " + (i + 1) + " | ");
						}
					}

					if (currentPage == lastPage - 1)
						out.print("Next");
					else
						out.print("<a href=\""
								+ link.replaceAll("\\{\\}", ""
										+ (currentPage + 1)) + "\"> Next </a>");
				}
			}
		} catch (IOException ioe) {
			System.out.println("Error in TablePagerTag: " + ioe);
		}
		return (SKIP_BODY);
	}

}