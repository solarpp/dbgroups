package com.douban.group;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

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
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

public class DownloadImage
{
	private static ArrayList<String> list;

	public int download(String theFirstPostURL, String theFirstPostName,
			String theFirstPostTitle)
	{
		System.out.println("analysing image links...");
		list = new ArrayList<String>();
		list.clear();
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(theFirstPostURL);
		HttpResponse response = null;
		HttpEntity entity = null;
		try
		{
			response = httpclient.execute(httpget);
			entity = response.getEntity();
			if (entity != null)
			{
				String responseString = EntityUtils.toString(entity, "UTF-8");
				Parser parser = new Parser(responseString);
				NodeList nodeList = parser.parse(null);
				processNodeList(nodeList);
				if (list.size() > 0)
				{
					saveImages(list, theFirstPostName, theFirstPostTitle);
					return list.size();
				}
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
		return 0;
	}

	private void processNodeList(NodeList nodelist)
	{
		SimpleNodeIterator iterator = nodelist.elements();
		while (iterator.hasMoreNodes())
		{
			Node node = iterator.nextNode();
			Tag tag = getTag(node);
			if (tag != null && tag.getAttribute("class") != null)
			{
				if (tag.getAttribute("class").startsWith("topic-figure"))
				{
					Node image = tag.getChildren().elementAt(1);
					if (image != null)
						list.add(getTag(image).getAttribute("src"));
				}
			}
			NodeList childList = node.getChildren();
			if (childList != null)
			{
				processNodeList(childList);
			} // end if
		}// end wile
	}

	private void saveImages(ArrayList<String> list, String theFirstPostName,
			String theFirstPostTitle)
	{
		System.out.println("downloading images...");
		int count = list.size();
		for (int i = 0; i < count; i++)
		{
			String imageURL = list.get(i);
			try
			{
				saveUrl(createFilename(imageURL, theFirstPostName,
						theFirstPostTitle), imageURL);
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private String createFilename(String imageURL, String theFirstPostName,
			String theFirstPostTitle)
	{
		String c = "/";
		int flag = imageURL.lastIndexOf(c);
		String filename = imageURL.substring(flag + 1);
		Calendar calendar = Calendar.getInstance();
		int m = calendar.get(Calendar.MONTH);
		int d = calendar.get(Calendar.DAY_OF_MONTH);
		int h = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		String[] splitedName = theFirstPostName.split("/");
		int count = splitedName.length;
		String poster = splitedName[count - 1];
		String path = DownloadImage.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath();
		return String.format("%s/%s_%s_%d_%d_%d_%d_%s", path, poster,
				theFirstPostTitle, m + 1, d, h, min, filename);
	}

	private void saveUrl(String filename, String urlString)
			throws MalformedURLException, IOException
	{
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try
		{
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1)
			{
				fout.write(data, 0, count);
			}
		} finally
		{
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
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
