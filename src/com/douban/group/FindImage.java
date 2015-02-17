package com.douban.group;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

public class FindImage
{
	private static String result;

	public String find(String targetURL)
	{
		System.out.println("finding image...");
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(targetURL);
		HttpResponse response = null;
		HttpEntity entity = null;
		try
		{
			response = httpclient.execute(httpget);
			System.out.println(response.getStatusLine().getStatusCode());
			entity = response.getEntity();
			if (entity != null)
			{
				String responseString = EntityUtils.toString(entity, "UTF-8");
				Parser parser = new Parser(responseString);
				// Parse all HTML node to nodeList
				NodeList nodeList = parser.parse(null);
				processNodeList(nodeList);
				if (result != null)
					return result;
			}
		} catch (ClientProtocolException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ParserException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private void processNodeList(NodeList nodeList)
	{
		// Iterate HTML element in nodeList.
		SimpleNodeIterator iterator = nodeList.elements();
		while (iterator.hasMoreNodes())
		{
			Node node = iterator.nextNode();
			TableTag tableTag = getTableTag(node);
			if (tableTag != null && tableTag.getAttribute("class") != null)
			{
				theFirstPostURL(node);
			} else
			{
				NodeList childList = node.getChildren();
				if (childList != null)
				{
					processNodeList(childList);
				} // end if
			}
		}// end wile
	}

	private void theFirstPostURL(Node node)
	{
		SimpleNodeIterator iterator = node.getChildren().elements();
		int up_count = 0, ad_count = 1;
		while (iterator.hasMoreNodes())
		{
			Node localnode = iterator.nextNode();
			TableRow rowTag = getTableRowTag(localnode);
			if (rowTag != null)
			{
				// Jump over the first row
				if (up_count == 0)
					up_count++;
				// Check if this is a UP row
				else if (rowTag.toHtml().contains(
						"http://img3.douban.com/pics/stick.gif"))
				{
					System.out.println(String.format("Up %d.", up_count));
					up_count++;
				}
				// Check if this is a AD row
				else if (rowTag.toHtml().contains("dale_group_special2"))
				{
					System.out.println(String.format("Ad %d.", ad_count));
					ad_count++;
				}
				// Here got the first user post
				else
				{
					TableColumn theFirstColumn = rowTag.getColumns()[0];
					Node url = theFirstColumn.childAt(1);
					TableColumn theSecondColumn = rowTag.getColumns()[1];
					Node name = theSecondColumn.childAt(0);
					result = getTag(url).getAttribute("href") + ","
							+ getTag(name).getAttribute("href") + ","
							+ getTag(url).getAttribute("title");
					break;
				}
			}
		}
	}

	private TableTag getTableTag(Node node)
	{
		if (node != null)
			try
			{
				return (TableTag) node;
			} catch (ClassCastException e)
			{
				return null;
			}
		else
			return null;
	}

	private TableRow getTableRowTag(Node node)
	{
		if (node != null)
			try
			{
				return (TableRow) node;
			} catch (ClassCastException e)
			{
				return null;
			}
		else
			return null;
	}

	private Tag getTag(Node node)
	{
		if (node != null)
			try
			{
				return (Tag) node;
			} catch (ClassCastException e)
			{
				return null;
			}
		else
			return null;
	}
}
